package pt.isep.sem4pi.infrastructure.bootstrap.airport;

import pt.isep.sem4pi.application.controller.addairport.AddAirportController;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

public class AirportBootstrapper {

    private final AddAirportController controller;

    public AirportBootstrapper(AirportRepository repository) {
        this.controller = new AddAirportController(repository);
    }

    public void execute() {
        System.out.println("Bootstrapping Airports...");

        registerSafely("Francisco Sa Carneiro", "Porto", "Portugal", "OPO", "LPPR", 41.24, -8.67, 69.0, "AREA_PT");
        registerSafely("Humberto Delgado", "Lisbon", "Portugal", "LIS", "LPPT", 38.77, -9.13, 114.0, "AREA_PT");
        registerSafely("John F. Kennedy Intl", "New York", "USA", "JFK", "KJFK", 40.64, -73.77, 4.0, "AREA_US_EAST");
        registerSafely("Adolfo Suarez Madrid-Barajas", "Madrid", "Spain", "MAD", "LEMD", 40.47, -3.56, 610.0, "AREA_ES");
        registerSafely("Heathrow", "London", "UK", "LHR", "EGLL", 51.47, -0.45, 25.0, "AREA_UK");
    }

    private void registerSafely(String name, String town, String country, String iata, String icao,
                                double lat, double lon, double elev, String areaId) {
        try {
            controller.addAirport(name, town, country, iata, icao, lat, lon, elev, areaId);
            System.out.println("   -> Bootstrapped: " + name);
        } catch (Exception e) {
            /* Imprimir a mensagem da exceção ajuda a perceber que regra de negócio falhou */
            System.out.println("   -> Skipped " + name + " (Reason: " + e.getMessage() + ")");
        }
    }
}