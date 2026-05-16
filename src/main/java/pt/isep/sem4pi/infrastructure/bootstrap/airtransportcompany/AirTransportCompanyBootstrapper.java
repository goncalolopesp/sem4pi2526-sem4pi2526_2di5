package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import pt.isep.sem4pi.application.controller.airtransportcompany.RegisterAirTransportCompanyController;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;

public class AirTransportCompanyBootstrapper {

    private final RegisterAirTransportCompanyController controller;

    public AirTransportCompanyBootstrapper(AirTransportCompanyRepository repository) {
        this.controller = new RegisterAirTransportCompanyController(repository);
    }

    public void execute() {
        System.out.println("Bootstrapping Air Transport Companies...");

        registerSafely("TAP Air Portugal", "TP", "TAP");
        registerSafely("American Airlines", "AA", "AAL");
        registerSafely("Ryanair", "FR", "RYR");
        registerSafely("Emirates", "EK", "UAE");
    }

    private void registerSafely(String name, String iata, String icao) {
        try {
            controller.registerAirTransportCompany(name, iata, icao);
            System.out.println("   -> Bootstrapped: " + name);
        } catch (Exception e) {
            System.out.println("   -> Skipped " + name + " (Already exists or invalid)");
        }
    }
}