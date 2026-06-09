package pt.isep.sem4pi.infrastructure.bootstrap.weatherdata;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.controller.weatherdata.RegisterWeatherController;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;

public class WeatherBootstrapper {
    private final RegisterWeatherController controller;

    public WeatherBootstrapper(WeatherBatchRepository repository) {
        this.controller = new RegisterWeatherController(
                repository,
                AuthzRegistry.authorizationService()
        );
    }

    public void execute() {
        System.out.println("Bootstrapping Manual Weather Data...");
        registerSafely("AREA_PT", 22.5, 12.0, 270.0, 55.0, 1015.0, 150.0);
    }

    private void registerSafely(String areaId, double t, double ws, double wd, double h, double p, double a) {
        try {
            controller.registerWeather(areaId, LocalDate.now(), t, ws, wd, h, p, a);
            System.out.println("   -> Bootstrapped Weather for: " + areaId);
        } catch (Exception e) {
            System.out.println("   -> Skipped Weather for " + areaId);
        }
    }
}