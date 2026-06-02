package pt.isep.sem4pi.domain.route;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;

import static org.junit.Assert.*;

public class FlightRouteIDTest {

    @Test
    public void ensureValidFlightRouteIDIsCreated() {
        FlightRouteID id = new FlightRouteID("TP123");
        assertNotNull(id);
    }

    @Test
    public void ensureCodeReturnsUpperCase() {
        FlightRouteID id = new FlightRouteID("tp1234");
        assertEquals("TP1234", id.code());
    }

    @Test
    public void ensureTwoLettersAndOneDigitIsAccepted() {
        FlightRouteID id = new FlightRouteID("AA1");
        assertEquals("AA1", id.code());
    }

    @Test
    public void ensureTwoLettersAndFourDigitsIsAccepted() {
        FlightRouteID id = new FlightRouteID("AA1234");
        assertEquals("AA1234", id.code());
    }

    @Test
    public void ensureLowerCaseIsConvertedToUpperCase() {
        FlightRouteID id = new FlightRouteID("ab99");
        assertEquals("AB99", id.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithNull() {
        new FlightRouteID(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithEmptyString() {
        new FlightRouteID("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithBlankString() {
        new FlightRouteID("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithOnlyLetters() {
        new FlightRouteID("ABC");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithOnlyDigits() {
        new FlightRouteID("1234");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithThreeLetters() {
        new FlightRouteID("ABC1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithOneLetterAndDigits() {
        new FlightRouteID("A123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithTwoLettersAndFiveDigits() {
        new FlightRouteID("AB12345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFlightRouteIDFailsWithSpecialCharacters() {
        new FlightRouteID("TP@1");
    }

    @Test
    public void ensureEqualsReturnsTrueForSameRouteID() {
        FlightRouteID id1 = new FlightRouteID("TP123");
        FlightRouteID id2 = new FlightRouteID("TP123");
        assertEquals(id1, id2);
    }

    @Test
    public void ensureEqualsReturnsFalseForDifferentRouteID() {
        FlightRouteID id1 = new FlightRouteID("TP123");
        FlightRouteID id2 = new FlightRouteID("TP456");
        assertNotEquals(id1, id2);
    }

    @Test
    public void ensureEqualsReturnsTrueForSameObject() {
        FlightRouteID id = new FlightRouteID("TP123");
        assertEquals(id, id);
    }

    @Test
    public void ensureEqualsReturnsFalseForNonFlightRouteID() {
        FlightRouteID id = new FlightRouteID("TP123");
        assertNotEquals(id, "TP123");
    }

    @Test
    public void ensureHashCodeIsSameForEqualObjects() {
        FlightRouteID id1 = new FlightRouteID("TP123");
        FlightRouteID id2 = new FlightRouteID("TP123");
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void ensureHashCodeDiffersForDifferentObjects() {
        FlightRouteID id1 = new FlightRouteID("TP123");
        FlightRouteID id2 = new FlightRouteID("TP456");
        assertNotEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void ensureCompareToReturnsZeroForEqualIDs() {
        FlightRouteID id1 = new FlightRouteID("TP123");
        FlightRouteID id2 = new FlightRouteID("TP123");
        assertEquals(0, id1.compareTo(id2));
    }

    @Test
    public void ensureCompareToReturnsNegativeForSmallerID() {
        FlightRouteID id1 = new FlightRouteID("AA11");
        FlightRouteID id2 = new FlightRouteID("ZZ99");
        assertTrue(id1.compareTo(id2) < 0);
    }

    @Test
    public void ensureCompareToReturnsPositiveForGreaterID() {
        FlightRouteID id1 = new FlightRouteID("ZZ99");
        FlightRouteID id2 = new FlightRouteID("AA11");
        assertTrue(id1.compareTo(id2) > 0);
    }

    @Test
    public void ensureToStringReturnsRouteID() {
        FlightRouteID id = new FlightRouteID("TP123");
        assertEquals("TP123", id.toString());
    }
}
