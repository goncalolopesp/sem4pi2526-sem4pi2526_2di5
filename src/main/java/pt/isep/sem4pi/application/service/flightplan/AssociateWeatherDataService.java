package pt.isep.sem4pi.application.service.flightplan;

import eapli.framework.application.ApplicationService;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.repositories.FlightPlanRepository;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinates the US082 association between flight plans and weather data.
 *
 * <p>The service performs cross-aggregate rules that do not belong inside
 * either aggregate: resolving both repositories and checking that a pilot
 * owns the selected plan. The {@link FlightPlan} aggregate remains responsible
 * for duplicate prevention and invalidating a previous test.</p>
 */
@ApplicationService
public class AssociateWeatherDataService {

    private final FlightPlanRepository flightPlanRepository;
    private final WeatherBatchRepository weatherRepository;

    public AssociateWeatherDataService(FlightPlanRepository flightPlanRepository,
                                       WeatherBatchRepository weatherRepository) {
        this.flightPlanRepository = flightPlanRepository;
        this.weatherRepository = weatherRepository;
    }

    public List<FlightPlan> listFlightPlansOwnedBy(String username) {
        return listFlightPlansAccessibleBy(username, false);
    }

    /**
     * Lists plans accessible by the current user.
     *
     * @param username authenticated username
     * @param canManageAll {@code true} for an administrator
     * @return plans owned by the pilot or all plans for an administrator
     */
    public List<FlightPlan> listFlightPlansAccessibleBy(String username, boolean canManageAll) {
        requireUsername(username);
        final List<FlightPlan> plans = new ArrayList<>();
        flightPlanRepository.findAll().forEach(plan -> {
            if (canManageAll || plan.pilot().isOwnedBy(username)) {
                plans.add(plan);
            }
        });
        return plans;
    }

    /**
     * Builds the weather-data selection list.
     *
     * <p>Exactly identical measurements are consolidated using a key composed
     * of area, interval and every measured value. This removes duplicate rows
     * imported in different batches without merging genuinely different
     * measurements from the same interval.</p>
     *
     * @return deduplicated and chronologically sorted selectable records
     */
    public List<WeatherDataViewDTO> listWeatherData() {
        final Map<String, WeatherDataViewDTO> uniqueWeatherData = new LinkedHashMap<>();
        weatherRepository.findAll().forEach(batch -> batch.getRecords().forEach(record ->
                uniqueWeatherData.putIfAbsent(weatherKey(batch.getAirControlAreaId(), record),
                        toViewDTO(batch.getAirControlAreaId(), record))));
        final List<WeatherDataViewDTO> weatherData = new ArrayList<>(uniqueWeatherData.values());
        weatherData.sort(Comparator.comparing(WeatherDataViewDTO::getDate)
                .thenComparing(WeatherDataViewDTO::getStartTime)
                .thenComparing(WeatherDataViewDTO::getAirControlAreaId));
        return weatherData;
    }

    /**
     * Associates each distinct selected weather entity and persists the flight
     * plan once after the complete operation.
     *
     * @param flightPlanId target plan identity
     * @param weatherDataIds selected persisted weather identities
     * @param username authenticated username
     * @param canManageAll whether ownership restrictions may be bypassed
     * @return association summary
     * @throws IllegalArgumentException when the selection is empty, an entity
     *                                  does not exist or ownership is invalid
     */
    public WeatherAssociationResult associateByIds(String flightPlanId, List<Long> weatherDataIds,
                                                   String username, boolean canManageAll) {
        requireUsername(username);
        if (weatherDataIds == null || weatherDataIds.isEmpty()) {
            throw new IllegalArgumentException("At least one weather record must be selected.");
        }

        final FlightPlan plan = findAccessiblePlan(flightPlanId, username, canManageAll);
        boolean invalidated = false;
        int associatedCount = 0;
        for (Long weatherDataId : weatherDataIds.stream().distinct().toList()) {
            final WeatherData weatherData = weatherRepository.findWeatherDataById(weatherDataId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Weather data not found: " + weatherDataId));
            final int previousSize = plan.weatherData().size();
            invalidated |= plan.associateWeatherData(weatherData);
            if (plan.weatherData().size() > previousSize) {
                associatedCount++;
            }
        }

        return new WeatherAssociationResult(
                flightPlanRepository.save(plan), invalidated, associatedCount);
    }

    public WeatherAssociationResult associate(String flightPlanId, String areaId, LocalDate date,
                                              LocalTime startTime, LocalTime endTime, String username) {
        return associate(flightPlanId, areaId, date, startTime, endTime, username, false);
    }

    public WeatherAssociationResult associate(String flightPlanId, String areaId, LocalDate date,
                                              LocalTime startTime, LocalTime endTime, String username,
                                              boolean canManageAll) {
        requireUsername(username);
        final FlightPlan plan = findAccessiblePlan(flightPlanId, username, canManageAll);

        final WeatherData weatherData = weatherRepository
                .findWeatherData(requireText(areaId, "Air Control Area ID"), date, startTime, endTime)
                .orElseThrow(() -> new IllegalArgumentException("Weather data not found."));

        final boolean invalidated = plan.associateWeatherData(weatherData);
        final FlightPlan savedPlan = flightPlanRepository.save(plan);
        return new WeatherAssociationResult(savedPlan, invalidated);
    }

    private WeatherDataViewDTO toViewDTO(String areaId, WeatherData record) {
        return new WeatherDataViewDTO(
                record.getWeatherDataId(), areaId, record.getDate(),
                record.getStartTime(), record.getEndTime(),
                record.getWind().getSpeedKnots(), record.getWind().getDirectionDegrees(),
                record.getTemperature().getCelsius(), record.getPressure().getHPa(),
                record.getHumidity().getPercentage(), record.getAltitude().getMeters());
    }

    private String weatherKey(String areaId, WeatherData record) {
        return String.join("|", areaId, record.getDate().toString(),
                record.getStartTime().toString(), record.getEndTime().toString(),
                Double.toString(record.getWind().getSpeedKnots()),
                Double.toString(record.getWind().getDirectionDegrees()),
                Double.toString(record.getTemperature().getCelsius()),
                Double.toString(record.getPressure().getHPa()),
                Double.toString(record.getHumidity().getPercentage()),
                Double.toString(record.getAltitude().getMeters()));
    }

    private FlightPlan findAccessiblePlan(String flightPlanId, String username,
                                          boolean canManageAll) {
        final FlightPlan plan = flightPlanRepository.ofIdentity(new FlightPlanId(flightPlanId))
                .orElseThrow(() -> new IllegalArgumentException("Flight plan not found."));
        if (!canManageAll && !plan.pilot().isOwnedBy(username)) {
            throw new IllegalArgumentException("You can only update your own flight plans.");
        }
        return plan;
    }

    private void requireUsername(String username) {
        requireText(username, "Authenticated pilot username");
    }

    private String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }
}
