package pt.isep.sem4pi.application.service.addairport;


import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.Elevation;
import pt.isep.sem4pi.domain.model.shared.IataCode;
import pt.isep.sem4pi.domain.model.shared.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

public class AddAirportService {

    private final AirportRepository airportRepository;

    public AddAirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Airport addAirport(String name, String town, String country, String rawIata, String rawIcao,
                              double lat, double lon, double elevMeters, String airControlAreaId) {

        IataCode iata = new IataCode(rawIata);
        IcaoCode icao = new IcaoCode(rawIcao);
        Coordinates coords = new Coordinates(lat, lon);
        Elevation elevation = new Elevation(elevMeters);

        if (airportRepository.findByIataCode(iata).isPresent()) {
            throw new IllegalStateException("An airport with this IATA code already exists.");
        }
        if (airportRepository.findByIcaoCode(icao).isPresent()) {
            throw new IllegalStateException("An airport with this ICAO code already exists.");
        }

        Airport newAirport = new Airport(name, town, country, iata, icao, coords, elevation, airControlAreaId);
        return airportRepository.save(newAirport);
    }
}