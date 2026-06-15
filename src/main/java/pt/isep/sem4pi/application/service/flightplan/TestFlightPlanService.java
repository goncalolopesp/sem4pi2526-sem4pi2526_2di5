package pt.isep.sem4pi.application.service.flightplan;

import eapli.framework.application.ApplicationService;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightTestExecutionResultDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.TestFlightPlanResultDTO;
import pt.isep.sem4pi.domain.model.flight.FlightLeg;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.flight.FlightPlanStatus;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;
import pt.isep.sem4pi.domain.repositories.AirportRepository;
import pt.isep.sem4pi.domain.repositories.FlightPlanRepository;
import pt.isep.sem4pi.infrastructure.simulation.FlightPlanJsonExporter;
import pt.isep.sem4pi.us081.US081FlightPlanMapper;
import pt.isep.sem4pi.us120.US120FlightPlanValidator;
import pt.isep.sem4pi.us120.US120ValidationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@ApplicationService
public class TestFlightPlanService {

    private static final double DEFAULT_MIN_ALTITUDE_METERS = 300.0;
    private static final int DEFAULT_STEP_SECONDS = 60;

    private final FlightPlanRepository flightPlanRepository;
    private final AirportRepository airportRepository;
    private final AirControlAreaRepository airControlAreaRepository;
    private final FlightTestRunner flightTestRunner;
    private final FlightPlanJsonExporter jsonExporter;

    public TestFlightPlanService(final FlightPlanRepository flightPlanRepository,
                                 final AirportRepository airportRepository,
                                 final AirControlAreaRepository airControlAreaRepository,
                                 final FlightTestRunner flightTestRunner) {
        this(
                flightPlanRepository,
                airportRepository,
                airControlAreaRepository,
                flightTestRunner,
                new FlightPlanJsonExporter(airportRepository)
        );
    }

    public TestFlightPlanService(final FlightPlanRepository flightPlanRepository,
                                 final AirportRepository airportRepository,
                                 final AirControlAreaRepository airControlAreaRepository,
                                 final FlightTestRunner flightTestRunner,
                                 final FlightPlanJsonExporter jsonExporter) {
        this.flightPlanRepository = flightPlanRepository;
        this.airportRepository = airportRepository;
        this.airControlAreaRepository = airControlAreaRepository;
        this.flightTestRunner = flightTestRunner;
        this.jsonExporter = jsonExporter;
    }

    public List<FlightPlan> listDraftFlightPlans(final String username, final boolean canManageAll) {
        requireUsername(username);
        final List<FlightPlan> drafts = new ArrayList<>();
        for (final FlightPlan plan : flightPlanRepository.findAll()) {
            if (plan.status() == FlightPlanStatus.DRAFT
                    && (canManageAll || plan.pilot().isOwnedBy(username))) {
                drafts.add(plan);
            }
        }
        return drafts;
    }

    public TestFlightPlanResultDTO testFlightPlan(final String flightPlanId,
                                                  final String username,
                                                  final boolean canManageAll) throws IOException {
        requireUsername(username);

        if (flightPlanId == null || flightPlanId.isBlank()) {
            throw new IllegalArgumentException("Flight plan id is required.");
        }

        final FlightPlan plan = flightPlanRepository.ofIdentity(new FlightPlanId(flightPlanId.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Flight plan not found."));

        if (plan.status() != FlightPlanStatus.DRAFT) {
            throw new IllegalStateException("Only draft flight plans can be tested.");
        }

        if (!canManageAll && !plan.pilot().isOwnedBy(username)) {
            throw new IllegalStateException("You can only test flight plans assigned to your pilot profile.");
        }

        final US120ValidationResult planValidation = validateDslFlightPlan(plan);
        if (!planValidation.isValid()) {
            plan.reject();
            flightPlanRepository.save(plan);
            return new TestFlightPlanResultDTO(
                    flightPlanId,
                    planValidation,
                    new FlightTestExecutionResultDTO(false, false, "", "Flight test skipped due to DSL validation errors."),
                    false,
                    "Flight plan rejected: DSL validation failed."
            );
        }

        final CreateFlightPlanDTO dslPlan = US081FlightPlanMapper.map(planValidation.getParseTree());
        final List<String> persistenceErrors = FlightPlanDslPersistenceComparator.compare(plan, dslPlan);
        if (!persistenceErrors.isEmpty()) {
            final US120ValidationResult persistenceValidation = new US120ValidationResult(
                    List.of(),
                    persistenceErrors,
                    planValidation.getParseTree()
            );
            plan.reject();
            flightPlanRepository.save(plan);
            return new TestFlightPlanResultDTO(
                    flightPlanId,
                    persistenceValidation,
                    new FlightTestExecutionResultDTO(false, false, "", "Flight test skipped due to persistence mismatch."),
                    false,
                    "Flight plan rejected: stored plan does not match DSL."
            );
        }

        final Path jsonFilePath = jsonExporter.export(plan, dslPlan);
        final double minReserveFuelKg = resolveMinimumReserveFuel(plan);
        final String reportPath = Path.of("scomp/reports", flightPlanId + "-test.txt").toString();

        final FlightTestExecutionResult testResult = flightTestRunner.run(
                jsonFilePath.toString(),
                DEFAULT_MIN_ALTITUDE_METERS,
                minReserveFuelKg,
                DEFAULT_STEP_SECONDS,
                reportPath
        );

        final FlightTestExecutionResultDTO testDto = new FlightTestExecutionResultDTO(
                true,
                testResult.success(),
                testResult.reportPath(),
                testResult.consoleOutput()
        );

        if (testResult.success()) {
            plan.validate();
            flightPlanRepository.save(plan);
            return new TestFlightPlanResultDTO(
                    flightPlanId,
                    planValidation,
                    testDto,
                    true,
                    "Flight plan validated successfully."
            );
        }

        plan.reject();
        flightPlanRepository.save(plan);
        return new TestFlightPlanResultDTO(
                flightPlanId,
                planValidation,
                testDto,
                false,
                "Flight plan rejected: C flight test failed."
        );
    }

    private US120ValidationResult validateDslFlightPlan(final FlightPlan plan) throws IOException {
        final String dslSourcePath = plan.dslSourcePath();
        if (dslSourcePath == null || dslSourcePath.isBlank()) {
            return new US120ValidationResult(
                    List.of(),
                    List.of("Flight plan has no associated DSL source file. Import the plan via US081."),
                    null
            );
        }

        final Path path = Path.of(dslSourcePath);
        if (!Files.exists(path)) {
            return new US120ValidationResult(
                    List.of(),
                    List.of("Associated DSL source file no longer exists: " + dslSourcePath),
                    null
            );
        }

        return US120FlightPlanValidator.analyze(path);
    }

    private double resolveMinimumReserveFuel(final FlightPlan plan) {
        final List<FlightLeg> legs = plan.legs();
        if (legs.isEmpty()) {
            return 0.0;
        }

        final IataCodeAirport destination = legs.get(legs.size() - 1).arrival().airport();
        final Airport airport = airportRepository.ofIdentity(destination)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination airport not found: " + destination.code()));

        final AirControlAreaID areaId = airport.getAirControlAreaId();
        final AirControlArea area = airControlAreaRepository.ofIdentity(areaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Air control area not found: " + areaId.getCode()));

        return area.getMinimumReserveFuelRule();
    }

    private void requireUsername(final String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Authenticated username is required.");
        }
    }
}
