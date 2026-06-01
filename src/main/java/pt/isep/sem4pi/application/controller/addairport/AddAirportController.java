package pt.isep.sem4pi.application.controller.addairport;

import pt.isep.sem4pi.application.service.addairport.AddAirportService;
import pt.isep.sem4pi.domain.airport.Airport;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

public class AddAirportController {
    private final AddAirportService service;

    public AddAirportController(AirportRepository airportRepository) {
        this.service = new AddAirportService(airportRepository);
    }

    public Airport addAirport(String name, String town, String country, String iata, String icao,
                              double lat, double lon, double elevMeters, String airControlAreaId) {
        return service.addAirport(name, town, country, iata, icao, lat, lon, elevMeters, airControlAreaId);
    }
}