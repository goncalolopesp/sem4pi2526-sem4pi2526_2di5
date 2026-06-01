package pt.isep.sem4pi.domain.route;

import org.junit.Test;

import static org.junit.Assert.*;

public class WindTest {

    @Test
    public void ensureValidWindIsCreated() {
        Wind wind = new Wind(180, 15.5);
        assertNotNull(wind);
    }

    @Test
    public void ensureWindWithZeroDirectionIsAccepted() {
        Wind wind = new Wind(0, 10.0);
        assertNotNull(wind);
    }

    @Test
    public void ensureWindWithMaxDirection359IsAccepted() {
        Wind wind = new Wind(359, 10.0);
        assertNotNull(wind);
    }

    @Test
    public void ensureWindWithZeroSpeedIsAccepted() {
        Wind wind = new Wind(90, 0.0);
        assertNotNull(wind);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureWindFailsWithNullDirection() {
        new Wind(null, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureWindFailsWithNullSpeed() {
        new Wind(180, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureWindFailsWithNegativeDirection() {
        new Wind(-1, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureWindFailsWithDirection360() {
        new Wind(360, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureWindFailsWithDirectionAbove360() {
        new Wind(400, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureWindFailsWithNegativeSpeed() {
        new Wind(180, -5.0);
    }

    @Test
    public void ensureWindWithHighSpeedIsAccepted() {
        Wind wind = new Wind(270, 999.99);
        assertNotNull(wind);
    }

    @Test
    public void ensureWindWithDirection1IsAccepted() {
        Wind wind = new Wind(1, 5.0);
        assertNotNull(wind);
    }
}
