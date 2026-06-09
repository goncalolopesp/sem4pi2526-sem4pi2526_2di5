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
import java.util.List;

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

    public List<WeatherDataViewDTO> listWeatherData() {
        final List<WeatherDataViewDTO> weatherData = new ArrayList<>();
        weatherRepository.findAll().forEach(batch -> batch.getRecords().forEach(record ->
                weatherData.add(toViewDTO(batch.getAirControlAreaId(), record))));
        weatherData.sort(Comparator.comparing(WeatherDataViewDTO::getDate)
                .thenComparing(WeatherDataViewDTO::getStartTime));
        return weatherData;
    }

    public WeatherAssociationResult associate(String flightPlanId, String areaId, LocalDate date,
                                              LocalTime startTime, LocalTime endTime, String username) {
        return associate(flightPlanId, areaId, date, startTime, endTime, username, false);
    }

    public WeatherAssociationResult associate(String flightPlanId, String areaId, LocalDate date,
                                              LocalTime startTime, LocalTime endTime, String username,
                                              boolean canManageAll) {
        requireUsername(username);
        final FlightPlan plan = flightPlanRepository.ofIdentity(new FlightPlanId(flightPlanId))
                .orElseThrow(() -> new IllegalArgumentException("Flight plan not found."));
        if (!canManageAll && !plan.pilot().isOwnedBy(username)) {
            throw new IllegalArgumentException("You can only update your own flight plans.");
        }

        final WeatherData weatherData = weatherRepository
                .findWeatherData(requireText(areaId, "Air Control Area ID"), date, startTime, endTime)
                .orElseThrow(() -> new IllegalArgumentException("Weather data not found."));

        final boolean invalidated = plan.associateWeatherData(weatherData);
        final FlightPlan savedPlan = flightPlanRepository.save(plan);
        return new WeatherAssociationResult(savedPlan, invalidated);
    }

    private WeatherDataViewDTO toViewDTO(String areaId, WeatherData record) {
        return new WeatherDataViewDTO(
                areaId, record.getDate(), record.getStartTime(), record.getEndTime(),
                record.getWind().getSpeedKnots(), record.getWind().getDirectionDegrees(),
                record.getTemperature().getCelsius(), record.getPressure().getHPa(),
                record.getHumidity().getPercentage(), record.getAltitude().getMeters());
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
