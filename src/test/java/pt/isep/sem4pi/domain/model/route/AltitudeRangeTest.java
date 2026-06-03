package pt.isep.sem4pi.domain.model.route;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;

import static org.junit.Assert.*;

/**
 * Test suite for AltitudeRange value object.
 *
 * Tests cover:
 * - Valid altitude range creation with various configurations
 * - Minimum altitude validation (non-negative)
 * - Maximum altitude validation (>= minimum)
 * - Width validation (positive values only)
 * - Null parameter detection
 * - Boundary conditions (zero minimum, equal min/max)
 * - Invalid values (negative min, max < min, zero/negative width)
 *
 * @author SEM4PI Team
 */
public class AltitudeRangeTest {

    /**
     * Test that a valid altitude range can be created.
     */
    @Test
    public void ensureValidAltitudeRangeIsCreated() {
        AltitudeRange range = new AltitudeRange(1000, 5000, 100.0);
        assertNotNull(range);
    }

    @Test
    public void ensureAltitudeRangeWithZeroMinIsAccepted() {
        AltitudeRange range = new AltitudeRange(0, 5000, 50.0);
        assertNotNull(range);
    }

    @Test
    public void ensureAltitudeRangeWithEqualMinMaxIsAccepted() {
        AltitudeRange range = new AltitudeRange(3000, 3000, 10.0);
        assertNotNull(range);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAltitudeRangeFailsWithNullMin() {
        new AltitudeRange(null, 5000, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAltitudeRangeFailsWithNullMax() {
        new AltitudeRange(1000, null, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAltitudeRangeFailsWithNullWidth() {
        new AltitudeRange(1000, 5000, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAltitudeRangeFailsWithNegativeMinAltitude() {
        new AltitudeRange(-1, 5000, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAltitudeRangeFailsWhenMinGreaterThanMax() {
        new AltitudeRange(6000, 5000, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAltitudeRangeFailsWithZeroWidth() {
        new AltitudeRange(1000, 5000, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAltitudeRangeFailsWithNegativeWidth() {
        new AltitudeRange(1000, 5000, -10.0);
    }

    @Test
    public void ensureAltitudeRangeWithSmallWidthIsAccepted() {
        AltitudeRange range = new AltitudeRange(100, 200, 0.01);
        assertNotNull(range);
    }

    @Test
    public void ensureAltitudeRangeWithLargeValuesIsAccepted() {
        AltitudeRange range = new AltitudeRange(0, 40000, 5000.0);
        assertNotNull(range);
    }
}
