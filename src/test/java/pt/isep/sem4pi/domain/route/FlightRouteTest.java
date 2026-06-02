package pt.isep.sem4pi.domain.route;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.route.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FlightRouteTest {

    private FlightRouteID validRouteID() {
        return new FlightRouteID("TP12");
    }

    private IataCodeCompany validCompanyCode() {
        return new IataCodeCompany("TP");
    }

    private IataCodeAirport validOriginAirport() {
        return new IataCodeAirport("OPO");
    }

    private IataCodeAirport validDestinationAirport() {
        return new IataCodeAirport("LIS");
    }

    private List<RouteSegment> validSegments() {
        Coordinates start = new Coordinates(41.15, -8.61);
        Coordinates end = new Coordinates(38.77, -9.13);
        Wind wind = new Wind(180, 15.0);
        AltitudeRange alt = new AltitudeRange(1000, 5000, 100.0);
        RouteSegment segment = new RouteSegment(start, end, Arrays.asList(alt), 500, wind);
        return Arrays.asList(segment);
    }

    @Test
    public void ensureValidFlightRouteIsCreated() {
        FlightRoute route = new FlightRoute(validRouteID(), validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), validSegments());
        assertNotNull(route);
    }

    @Test
    public void ensureIdentityReturnsRouteID() {
        FlightRoute route = new FlightRoute(validRouteID(), validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), validSegments());
        assertEquals(validRouteID(), route.identity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteFailsWithNullRouteID() {
        new FlightRoute(null, validCompanyCode(), validOriginAirport(),
                validDestinationAirport(), validSegments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteFailsWithNullCompanyCode() {
        new FlightRoute(validRouteID(), null, validOriginAirport(),
                validDestinationAirport(), validSegments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteFailsWithNullOriginAirport() {
        new FlightRoute(validRouteID(), validCompanyCode(), null,
                validDestinationAirport(), validSegments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteFailsWithNullDestinationAirport() {
        new FlightRoute(validRouteID(), validCompanyCode(), validOriginAirport(),
                null, validSegments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteFailsWithNullSegments() {
        new FlightRoute(validRouteID(), validCompanyCode(), validOriginAirport(),
                validDestinationAirport(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteFailsWithEmptySegments() {
        new FlightRoute(validRouteID(), validCompanyCode(), validOriginAirport(),
                validDestinationAirport(), new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteFailsWhenRouteNameDoesNotStartWithCompanyCode() {
        FlightRouteID routeID = new FlightRouteID("AB12");
        IataCodeCompany companyCode = new IataCodeCompany("TP");
        new FlightRoute(routeID, companyCode, validOriginAirport(),
                validDestinationAirport(), validSegments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteFailsWithInvalidRouteNameFormat_OnlyLetters() {
        // This should fail at FlightRouteID constructor level
        FlightRouteID routeID = new FlightRouteID("TPAB");
        IataCodeCompany companyCode = new IataCodeCompany("TP");
        new FlightRoute(routeID, companyCode, validOriginAirport(),
                validDestinationAirport(), validSegments());
    }

    @Test
    public void ensureFlightRouteWithMultipleSegmentsIsCreated() {
        Coordinates start1 = new Coordinates(41.15, -8.61);
        Coordinates end1 = new Coordinates(40.0, -8.0);
        Coordinates start2 = new Coordinates(40.0, -8.0);
        Coordinates end2 = new Coordinates(38.77, -9.13);
        Wind wind = new Wind(90, 10.0);
        AltitudeRange alt = new AltitudeRange(2000, 8000, 200.0);
        RouteSegment seg1 = new RouteSegment(start1, end1, Arrays.asList(alt), 600, wind);
        RouteSegment seg2 = new RouteSegment(start2, end2, Arrays.asList(alt), 600, wind);

        FlightRoute route = new FlightRoute(validRouteID(), validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), Arrays.asList(seg1, seg2));
        assertNotNull(route);
    }

    @Test
    public void ensureSameAsReturnsTrueForSameReference() {
        FlightRoute route = new FlightRoute(validRouteID(), validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), validSegments());
        assertTrue(route.sameAs(route));
    }

    @Test
    public void ensureSameAsReturnsFalseForNonFlightRoute() {
        FlightRoute route = new FlightRoute(validRouteID(), validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), validSegments());
        assertFalse(route.sameAs("not a flight route"));
    }

    @Test
    public void ensureSameAsReturnsTrueForSameIdentity() {
        FlightRoute route1 = new FlightRoute(validRouteID(), validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), validSegments());
        FlightRoute route2 = new FlightRoute(validRouteID(), validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), validSegments());
        assertTrue(route1.sameAs(route2));
    }

    @Test
    public void ensureSameAsReturnsFalseForDifferentIdentity() {
        FlightRoute route1 = new FlightRoute(new FlightRouteID("TP12"), new IataCodeCompany("TP"),
                validOriginAirport(), validDestinationAirport(), validSegments());
        FlightRoute route2 = new FlightRoute(new FlightRouteID("TP99"), new IataCodeCompany("TP"),
                validOriginAirport(), validDestinationAirport(), validSegments());
        assertFalse(route1.sameAs(route2));
    }

    @Test
    public void ensureRouteWithTwoLettersAndOneDigitIsAccepted() {
        FlightRouteID routeID = new FlightRouteID("TP1");
        FlightRoute route = new FlightRoute(routeID, validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), validSegments());
        assertNotNull(route);
    }

    @Test
    public void ensureRouteWithTwoLettersAndFourDigitsIsAccepted() {
        FlightRouteID routeID = new FlightRouteID("TP1234");
        FlightRoute route = new FlightRoute(routeID, validCompanyCode(),
                validOriginAirport(), validDestinationAirport(), validSegments());
        assertNotNull(route);
    }
}
