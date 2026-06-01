package pt.isep.sem4pi.application.service.addairport;

import pt.isep.sem4pi.domain.airport.Airport;
import pt.isep.sem4pi.domain.airport.Coordinates;
import pt.isep.sem4pi.domain.airport.Elevation;
import pt.isep.sem4pi.domain.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.airport.IcaoCodeAirport;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

public class AddAirportService {

    private final AirportRepository airportRepository;

    public AddAirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Airport addAirport(String name, String town, String country, String rawIata, String rawIcao,
                              double lat, double lon, double elevMeters, String rawAirControlAreaId) {

        IataCodeAirport iata = new IataCodeAirport(rawIata);
        IcaoCodeAirport icao = new IcaoCodeAirport(rawIcao);
        Coordinates coords = new Coordinates(lat, lon);
        Elevation elevation = new Elevation(elevMeters);
        AirControlAreaID airControlAreaId = new AirControlAreaID(rawAirControlAreaId);

        if (airportRepository.ofIdentity(iata).isPresent()) {
            throw new IllegalStateException("An airport with this IATA code already exists.");
        }
        if (airportRepository.findByIcaoCode(icao).isPresent()) {
            throw new IllegalStateException("An airport with this ICAO code already exists.");
        }

        Airport newAirport = new Airport(name, town, country, iata, icao, coords, elevation, airControlAreaId);

        return airportRepository.save(newAirport);
    }
}