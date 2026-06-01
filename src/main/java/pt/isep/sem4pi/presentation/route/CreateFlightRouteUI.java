package pt.isep.sem4pi.presentation.route;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.route.CreateFlightRouteController;
import pt.isep.sem4pi.domain.route.DTO.*;
import pt.isep.sem4pi.domain.airport.DTO.CoordinatesDTO;
import pt.isep.sem4pi.domain.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.airport.Airport;

import java.util.ArrayList;
import java.util.List;

/**
 * User interface for creating a new flight route.
 */
public class CreateFlightRouteUI extends AbstractUI {

    private final CreateFlightRouteController controller = new CreateFlightRouteController();

    @Override
    protected boolean doShow() {
        /// 1. Choose Air Transport Company
        Iterable<AirTransportCompany> companiesIt = controller.getCompanies();
        List<AirTransportCompany> companies = new ArrayList<>();
        companiesIt.forEach(companies::add);

        if (companies.isEmpty()) {
            System.out.println("[ERROR] No Air Transport Companies available.");
            return false;
        }

        System.out.println("Air Transport Companies:");
        for (int i = 0; i < companies.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, companies.get(i).identity().toString());
        }
        System.out.println("0. Exit");
        int compChoice = Console.readInteger("Select an option: ");
        if (compChoice == 0) return false;
        if (compChoice < 1 || compChoice > companies.size()) {
            System.out.println("Invalid option. Operation cancelled.");
            return false;
        }
        AirTransportCompany selectedCompany = companies.get(compChoice - 1);

        /// 2. Basic Route Info
        String routeID = Console.readLine("Route Name/ID (e.g. " + selectedCompany.identity().toString() + "123): ");

        /// 3. Choose Airports
        Iterable<Airport> airportsIt = controller.getAirports();
        List<Airport> airports = new ArrayList<>();
        airportsIt.forEach(airports::add);

        if (airports.size() < 2) {
            System.out.println("[ERROR] Not enough airports registered to create a route.");
            return false;
        }

        Airport originAirport = selectAirport(airports, "Origin");
        if (originAirport == null) return false;

        Airport destAirport = selectAirport(airports, "Destination");
        if (destAirport == null) return false;

        if (originAirport.identity().equals(destAirport.identity())) {
            System.out.println("[ERROR] Origin and Destination airports cannot be the same.");
            return false;
        }

        /// 4. Collect Segments
        List<RouteSegmentDTO> segments = new ArrayList<>();
        boolean addSegment = true;
        int segmentCount = 1;

        while (addSegment) {
            System.out.println("\n--- Adding Segment " + segmentCount + " ---");

            System.out.println("Start Node:");
            Double startLat = readLatitude();
            Double startLon = readLongitude();

            System.out.println("End Node:");
            Double endLat = readLatitude();
            Double endLon = readLongitude();

            Integer windDir = readWindDirection();
            Double windSpeed = readPositiveDouble("Wind Speed (m/s): ");
            Integer maxSpeed = readPositiveInteger("Max Speed for this segment: ");

            // Collect Altitude Slots for this segment
            List<AltitudeRangeDTO> altitudes = new ArrayList<>();
            boolean addAltitude = true;
            while (addAltitude) {
                System.out.println("- Adding Altitude Slot -");
                Integer minAlt = readPositiveInteger("Minimum Altitude: ");
                Integer maxAlt = readMaxAltitude(minAlt);
                Double width = readPositiveDouble("Slot Width: ");

                altitudes.add(new AltitudeRangeDTO(minAlt, maxAlt, width));

                addAltitude = Console.readBoolean("Add another altitude slot for this segment? (y/n): ");
            }

            segments.add(new RouteSegmentDTO(null,
                    new CoordinatesDTO(startLat, startLon),
                    new CoordinatesDTO(endLat, endLon),
                    new WindDTO(windDir, windSpeed),
                    maxSpeed,
                    altitudes));

            System.out.println("Segment added.");
            addSegment = Console.readBoolean("Add another segment to the route? (y/n): ");
            segmentCount++;
        }

        /// 5. Build DTO and send to Controller
        FlightRouteDTO dto = new FlightRouteDTO(
                routeID,
                selectedCompany.identity().toString(),
                originAirport.identity().toString(),
                destAirport.identity().toString(),
                segments,
                null
        );

        try {
            controller.createFlightRoute(dto);
            System.out.println("\nFlight Route " + routeID + " successfully created!");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nUNEXPECTED ERROR: " + e.getMessage());
        }

        return false;
    }

    /**
     * Helper methods to enforce data rules during UI input.
     */
    private Airport selectAirport(List<Airport> airports, String type) {
        System.out.println("\nSelect " + type + " Airport:");
        for (int i = 0; i < airports.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, airports.get(i).identity().toString());
        }
        System.out.println("0. Exit");
        int choice = Console.readInteger("Select an option: ");
        if (choice == 0) return null;
        if (choice < 1 || choice > airports.size()) {
            System.out.println("Invalid option.");
            return null;
        }
        return airports.get(choice - 1);
    }

    private Double readLatitude() {
        while (true) {
            double val = Console.readDouble("Latitude (-90 to 90): ");
            if (val >= -90.0 && val <= 90.0) return val;
            System.out.println("[ERROR] Latitude must be between -90 and 90.");
        }
    }

    private Double readLongitude() {
        while (true) {
            double val = Console.readDouble("Longitude (-180 to 180): ");
            if (val >= -180.0 && val <= 180.0) return val;
            System.out.println("[ERROR] Longitude must be between -180 and 180.");
        }
    }

    private Integer readWindDirection() {
        while (true) {
            int val = Console.readInteger("Wind Direction (0 to 359 degrees): ");
            if (val >= 0 && val < 360) return val;
            System.out.println("[ERROR] Wind direction must be between 0 and 359.");
        }
    }

    private Double readPositiveDouble(String prompt) {
        while (true) {
            double val = Console.readDouble(prompt);
            if (val > 0) return val;
            System.out.println("[ERROR] Value must be positive.");
        }
    }

    private Integer readPositiveInteger(String prompt) {
        while (true) {
            int val = Console.readInteger(prompt);
            if (val >= 0) return val;
            System.out.println("[ERROR] Value cannot be negative.");
        }
    }

    private Integer readMaxAltitude(Integer minAlt) {
        while (true) {
            int val = Console.readInteger("Maximum Altitude: ");
            if (val >= minAlt) return val;
            System.out.println("[ERROR] Max altitude cannot be lower than min altitude (" + minAlt + ").");
        }
    }

    @Override
    public String headline() {
        return "Create Flight Route";
    }
}
