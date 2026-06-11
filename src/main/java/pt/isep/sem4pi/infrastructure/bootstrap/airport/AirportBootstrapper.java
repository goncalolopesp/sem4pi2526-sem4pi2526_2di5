package pt.isep.sem4pi.infrastructure.bootstrap.airport;

import pt.isep.sem4pi.application.controller.addairport.AddAirportController;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

public class AirportBootstrapper {

    private final AddAirportController controller;
    private final AirportRepository repository;

    public AirportBootstrapper(final AirportRepository repository) {
        this.repository = repository;
        this.controller = new AddAirportController(repository);
    }

    public void execute() {
        System.out.println("Bootstrapping Airports...");

        registerSafely("Francisco Sa Carneiro", "Porto", "Portugal", "OPO", "LPPR", 41.24, -8.67, 69.0, "LIS_FIR");
        registerSafely("Humberto Delgado", "Lisbon", "Portugal", "LIS", "LPPT", 38.77, -9.13, 114.0, "LIS_FIR");
        registerSafely("John F. Kennedy Intl", "New York", "USA", "JFK", "KJFK", 40.64, -73.77, 4.0, "JFK_FIR");
        registerSafely("Adolfo Suarez Madrid-Barajas", "Madrid", "Spain", "MAD", "LEMD", 40.47, -3.56, 610.0, "MAD_FIR");
        registerSafely("Heathrow", "London", "UK", "LHR", "EGLL", 51.47, -0.45, 25.0, "UK_FIR");

        repairAirControlArea("OPO", "LIS_FIR");
        repairAirControlArea("LIS", "LIS_FIR");
        repairAirControlArea("JFK", "JFK_FIR");
        repairAirControlArea("MAD", "MAD_FIR");
        repairAirControlArea("LHR", "UK_FIR");
    }

    private void registerSafely(final String name, final String town, final String country, final String iata,
                                  final String icao, final double lat, final double lon, final double elev,
                                  final String areaId) {
        try {
            controller.addAirport(name, town, country, iata, icao, lat, lon, elev, areaId);
            System.out.println("   -> Bootstrapped: " + name);
        } catch (Exception e) {
            System.out.println("   -> Skipped " + name + " (Reason: " + e.getMessage() + ")");
        }
    }

    private void repairAirControlArea(final String iataCode, final String expectedAreaCode) {
        repository.ofIdentity(new IataCodeAirport(iataCode)).ifPresent(airport -> {
            final String currentAreaCode = airport.getAirControlAreaId().getCode();
            if (!expectedAreaCode.equalsIgnoreCase(currentAreaCode)) {
                airport.updateAirControlArea(new AirControlAreaID(expectedAreaCode));
                repository.save(airport);
                System.out.println("   -> Repaired airport " + iataCode
                        + " air control area: " + currentAreaCode + " -> " + expectedAreaCode);
            }
        });
    }
}
