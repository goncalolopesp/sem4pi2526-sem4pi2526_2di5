package pt.isep.sem4pi.application.viadto;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.airport.DTO.CoordinatesDTO;
import pt.isep.sem4pi.domain.model.route.DTO.AltitudeRangeDTO;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.model.route.DTO.RouteSegmentDTO;
import pt.isep.sem4pi.domain.model.route.DTO.WindDTO;

import static org.junit.Assert.*;

public class FlightRouteDTORepresentationBuilderTest {

    private FlightRouteDTORepresentationBuilder builder;

    @Before
    public void setUp() {
        builder = new FlightRouteDTORepresentationBuilder();
    }

    // --- build() ---

    @Test
    public void ensureBuildReturnsFlightRouteDTO() {
        FlightRouteDTO dto = builder.build();
        assertNotNull(dto);
    }

    @Test
    public void ensureBuildResetsInternalState() {
        builder.startObject("FlightRoute");
        builder.withProperty("routeID", "TP12");
        FlightRouteDTO first = builder.build();
        FlightRouteDTO second = builder.build();

        assertEquals("TP12", first.getRouteID());
        assertNull(second.getRouteID());
    }

    // --- startObject / withProperty(String) for FlightRoute ---

    @Test
    public void ensureFlightRoutePropertiesAreSetCorrectly() {
        builder.startObject("FlightRoute");
        builder.withProperty("routeID", "TP12");
        builder.withProperty("companyCode", "TP");
        builder.withProperty("originAirport", "OPO");
        builder.withProperty("destinationAirport", "LIS");

        FlightRouteDTO dto = builder.build();
        assertEquals("TP12", dto.getRouteID());
        assertEquals("TP", dto.getCompanyCode());
        assertEquals("OPO", dto.getOriginAirport());
        assertEquals("LIS", dto.getDestinationAirport());
    }

    // --- startObject("segment") ---

    @Test
    public void ensureSegmentObjectIsCreated() {
        builder.startObject("FlightRoute");
        builder.startObject("segment");
        builder.withProperty("maxSpeed", 500);
        builder.endObject(); // end segment

        FlightRouteDTO dto = builder.build();
        assertNotNull(dto.getSegments());
        assertEquals(1, dto.getSegments().size());
        assertEquals(Integer.valueOf(500), dto.getSegments().get(0).getMaxSpeed());
    }

    // --- startObject("startNode") and ("endNode") ---

    @Test
    public void ensureStartNodeCoordinatesAreSetCorrectly() {
        builder.startObject("FlightRoute");
        builder.startObject("segment");

        builder.startObject("startNode");
        builder.withProperty("latitude", 41.15);
        builder.withProperty("longitude", -8.61);
        builder.endObject(); // end startNode

        builder.endObject(); // end segment

        FlightRouteDTO dto = builder.build();
        CoordinatesDTO startNode = dto.getSegments().get(0).getStartNode();
        assertNotNull(startNode);
        assertEquals(41.15, startNode.getLatitude(), 0.001);
        assertEquals(-8.61, startNode.getLongitude(), 0.001);
    }

    @Test
    public void ensureEndNodeCoordinatesAreSetCorrectly() {
        builder.startObject("FlightRoute");
        builder.startObject("segment");

        builder.startObject("endNode");
        builder.withProperty("latitude", 38.77);
        builder.withProperty("longitude", -9.13);
        builder.endObject(); // end endNode

        builder.endObject(); // end segment

        FlightRouteDTO dto = builder.build();
        CoordinatesDTO endNode = dto.getSegments().get(0).getEndNode();
        assertNotNull(endNode);
        assertEquals(38.77, endNode.getLatitude(), 0.001);
        assertEquals(-9.13, endNode.getLongitude(), 0.001);
    }

    // --- startObject("wind") ---

    @Test
    public void ensureWindPropertiesAreSetCorrectly() {
        builder.startObject("FlightRoute");
        builder.startObject("segment");

        builder.startObject("wind");
        builder.withProperty("directionAngle", 180);
        builder.withProperty("speed", 15.5);
        builder.endObject(); // end wind

        builder.endObject(); // end segment

        FlightRouteDTO dto = builder.build();
        WindDTO wind = dto.getSegments().get(0).getWind();
        assertNotNull(wind);
        assertEquals(Integer.valueOf(180), wind.getDirectionAngle());
        assertEquals(15.5, wind.getSpeed(), 0.001);
    }

    // --- startObject("altitudeSlot") ---

    @Test
    public void ensureAltitudeSlotIsAddedToSegment() {
        builder.startObject("FlightRoute");
        builder.startObject("segment");

        builder.startObject("altitudeSlot");
        builder.withProperty("minAltitude", 1000);
        builder.withProperty("maxAltitude", 5000);
        builder.withProperty("width", 100.0);
        builder.endObject(); // end altitudeSlot

        builder.endObject(); // end segment

        FlightRouteDTO dto = builder.build();
        assertNotNull(dto.getSegments().get(0).getAllowedAltitudes());
        assertEquals(1, dto.getSegments().get(0).getAllowedAltitudes().size());
        AltitudeRangeDTO alt = dto.getSegments().get(0).getAllowedAltitudes().get(0);
        assertEquals(Integer.valueOf(1000), alt.getMinAltitude());
        assertEquals(Integer.valueOf(5000), alt.getMaxAltitude());
        assertEquals(100.0, alt.getWidth(), 0.001);
    }

    @Test
    public void ensureMultipleAltitudeSlotsCanBeAdded() {
        builder.startObject("FlightRoute");
        builder.startObject("segment");

        builder.startObject("altitudeSlot");
        builder.withProperty("minAltitude", 1000);
        builder.withProperty("maxAltitude", 3000);
        builder.withProperty("width", 50.0);
        builder.endObject(); // end first altitudeSlot

        builder.startObject("altitudeSlot");
        builder.withProperty("minAltitude", 3000);
        builder.withProperty("maxAltitude", 5000);
        builder.withProperty("width", 100.0);
        builder.endObject(); // end second altitudeSlot

        builder.endObject(); // end segment

        FlightRouteDTO dto = builder.build();
        assertEquals(2, dto.getSegments().get(0).getAllowedAltitudes().size());
    }

    // --- withProperty(String, Long) ---

    @Test
    public void ensureVersionIsSet() {
        builder.startObject("FlightRoute");
        builder.withProperty("version", 1L);

        FlightRouteDTO dto = builder.build();
        assertEquals(Long.valueOf(1), dto.getVersion());
    }

    @Test
    public void ensureSegmentIdIsSet() {
        builder.startObject("FlightRoute");
        builder.startObject("segment");
        builder.withProperty("segmentId", 42L);
        builder.endObject(); // end segment

        FlightRouteDTO dto = builder.build();
        assertEquals(Long.valueOf(42), dto.getSegments().get(0).getSegmentId());
    }

    // --- startCollection / endCollection ---

    @Test
    public void ensureStartCollectionInitializesSegmentsList() {
        builder.startCollection("segments");

        FlightRouteDTO dto = builder.build();
        assertNotNull(dto.getSegments());
        assertTrue(dto.getSegments().isEmpty());
    }

    @Test
    public void ensureStartCollectionDoesNotOverwriteExistingList() {
        builder.startObject("FlightRoute");
        builder.startObject("segment");
        builder.withProperty("maxSpeed", 500);
        builder.endObject(); // end segment

        builder.startCollection("segments"); // should NOT overwrite

        FlightRouteDTO dto = builder.build();
        assertEquals(1, dto.getSegments().size());
    }

    @Test
    public void ensureEndCollectionReturnsSelf() {
        assertNotNull(builder.endCollection());
    }

    // --- withElement ---

    @Test
    public void ensureWithElementDoesNotCrash() {
        // withElement is a no-op (logs a warning)
        builder.withElement("some_value");
        FlightRouteDTO dto = builder.build();
        assertNotNull(dto);
    }

    // --- endObject for non-matching states ---

    @Test
    public void ensureEndObjectWithUnknownChildResetsState() {
        builder.startObject("unknownObject");
        builder.endObject();

        FlightRouteDTO dto = builder.build();
        assertNotNull(dto);
    }

    // --- Full integration flow ---

    @Test
    public void ensureFullBuildFlowProducesCorrectDTO() {
        builder.startObject("FlightRoute");
        builder.withProperty("routeID", "TP12");
        builder.withProperty("companyCode", "TP");
        builder.withProperty("originAirport", "OPO");
        builder.withProperty("destinationAirport", "LIS");
        builder.withProperty("version", 1L);

        builder.startCollection("segments");

        builder.startObject("segment");
        builder.withProperty("segmentId", 1L);
        builder.withProperty("maxSpeed", 600);

        builder.startObject("startNode");
        builder.withProperty("latitude", 41.15);
        builder.withProperty("longitude", -8.61);
        builder.endObject();

        builder.startObject("endNode");
        builder.withProperty("latitude", 38.77);
        builder.withProperty("longitude", -9.13);
        builder.endObject();

        builder.startObject("wind");
        builder.withProperty("directionAngle", 180);
        builder.withProperty("speed", 15.5);
        builder.endObject();

        builder.startObject("altitudeSlot");
        builder.withProperty("minAltitude", 2000);
        builder.withProperty("maxAltitude", 8000);
        builder.withProperty("width", 200.0);
        builder.endObject();

        builder.endObject(); // end segment
        builder.endCollection();

        FlightRouteDTO dto = builder.build();

        assertEquals("TP12", dto.getRouteID());
        assertEquals("TP", dto.getCompanyCode());
        assertEquals("OPO", dto.getOriginAirport());
        assertEquals("LIS", dto.getDestinationAirport());
        assertEquals(Long.valueOf(1), dto.getVersion());
        assertEquals(1, dto.getSegments().size());

        RouteSegmentDTO seg = dto.getSegments().get(0);
        assertEquals(Long.valueOf(1), seg.getSegmentId());
        assertEquals(Integer.valueOf(600), seg.getMaxSpeed());
        assertEquals(41.15, seg.getStartNode().getLatitude(), 0.001);
        assertEquals(-9.13, seg.getEndNode().getLongitude(), 0.001);
        assertEquals(Integer.valueOf(180), seg.getWind().getDirectionAngle());
        assertEquals(1, seg.getAllowedAltitudes().size());
    }

    // --- withProperty(String) with non-FlightRoute context should be ignored ---

    @Test
    public void ensureWithPropertyStringIsIgnoredForNonFlightRouteContext() {
        builder.startObject("segment");
        builder.withProperty("routeID", "TP12"); // should not set anything on dto

        FlightRouteDTO dto = builder.build();
        assertNull(dto.getRouteID());
    }

    // --- withProperty(Double) with non-matching context ---

    @Test
    public void ensureWithPropertyDoubleIsIgnoredForNonMatchingContext() {
        builder.startObject("FlightRoute");
        builder.withProperty("latitude", 41.15); // FlightRoute context, not startNode

        FlightRouteDTO dto = builder.build();
        assertNotNull(dto);
    }

    // --- withProperty(Integer) with non-matching context ---

    @Test
    public void ensureWithPropertyIntegerIsIgnoredForNonMatchingContext() {
        builder.startObject("FlightRoute");
        builder.withProperty("maxSpeed", 500); // FlightRoute context, not segment

        FlightRouteDTO dto = builder.build();
        assertNotNull(dto);
    }

    // --- Multiple segments ---

    @Test
    public void ensureMultipleSegmentsAreAdded() {
        builder.startObject("FlightRoute");

        builder.startObject("segment");
        builder.withProperty("maxSpeed", 500);
        builder.endObject();

        builder.startObject("segment");
        builder.withProperty("maxSpeed", 700);
        builder.endObject();

        FlightRouteDTO dto = builder.build();
        assertEquals(2, dto.getSegments().size());
        assertEquals(Integer.valueOf(500), dto.getSegments().get(0).getMaxSpeed());
        assertEquals(Integer.valueOf(700), dto.getSegments().get(1).getMaxSpeed());
    }
}
