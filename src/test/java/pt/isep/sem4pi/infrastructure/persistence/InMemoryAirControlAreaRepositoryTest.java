package pt.isep.sem4pi.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaBoundary;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirControlAreaRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class InMemoryAirControlAreaRepositoryTest {

    private InMemoryAirControlAreaRepository repo;

    @Before
    public void setUp() {
        repo = new InMemoryAirControlAreaRepository();

        List<AirControlArea> toDelete = new ArrayList<>();
        repo.findAll().forEach(toDelete::add);

        for (AirControlArea area : toDelete) {
            repo.delete(area);
        }
    }

    @Test
    public void ensureSavedAreaCanBeFoundByIdentity() {
        final AirControlArea area = new AirControlArea(
                "LIS_FIR",
                new AirControlAreaBoundary(36.0, 42.0, -10.0, -6.0),
                "Lisbon FIR",
                1000.0
        );

        repo.save(area);

        assertTrue(repo.containsOfIdentity(new AirControlAreaID("LIS_FIR")));
    }

    @Test
    public void ensureUnknownIdIsNotFound() {
        assertFalse(repo.containsOfIdentity(new AirControlAreaID("EDGG")));
    }

    @Test
    public void ensureSavingTwoDistinctAreasStoresBoth() {
        repo.save(new AirControlArea(
                "LIS_FIR",
                new AirControlAreaBoundary(36.0, 42.0, -10.0, -6.0),
                "Lisbon FIR",
                1000.0
        ));
        repo.save(new AirControlArea(
                "MAD_FIR",
                new AirControlAreaBoundary(35.0, 43.0, -5.0, 0.0),
                "Madrid FIR",
                1200.0
        ));

        assertTrue(repo.containsOfIdentity(new AirControlAreaID("LIS_FIR")));
        assertTrue(repo.containsOfIdentity(new AirControlAreaID("MAD_FIR")));
    }

    @Test
    public void ensureDeletedAreaIsNoLongerFound() {
        final AirControlArea area = new AirControlArea(
                "LIS_FIR",
                new AirControlAreaBoundary(36.0, 42.0, -10.0, -6.0),
                "Lisbon FIR",
                1000.0
        );

        repo.save(area);
        repo.delete(area);

        assertFalse(repo.containsOfIdentity(new AirControlAreaID("LIS_FIR")));
    }

    @Test
    public void ensureDeleteOfIdentityRemovesCorrectArea() {
        repo.save(new AirControlArea(
                "LIS_FIR",
                new AirControlAreaBoundary(36.0, 42.0, -10.0, -6.0),
                "Lisbon FIR",
                1000.0
        ));
        repo.save(new AirControlArea(
                "MAD_FIR",
                new AirControlAreaBoundary(35.0, 43.0, -5.0, 0.0),
                "Madrid FIR",
                1200.0
        ));

        repo.deleteOfIdentity(new AirControlAreaID("LIS_FIR"));

        assertFalse(repo.containsOfIdentity(new AirControlAreaID("LIS_FIR")));
        assertTrue(repo.containsOfIdentity(new AirControlAreaID("MAD_FIR")));
    }

    @Test
    public void ensureCountIsZeroOnEmptyRepository() {
        assertEquals(0, repo.count());
    }

    @Test
    public void ensureCountReflectsNumberOfSavedAreas() {
        repo.save(new AirControlArea(
                "LIS_FIR",
                new AirControlAreaBoundary(36.0, 42.0, -10.0, -6.0),
                "Lisbon FIR",
                1000.0
        ));
        repo.save(new AirControlArea(
                "MAD_FIR",
                new AirControlAreaBoundary(35.0, 43.0, -5.0, 0.0),
                "Madrid FIR",
                1200.0
        ));

        assertEquals(2, repo.count());
    }

    @Test
    public void ensureFindAllReturnsAllSavedAreas() {
        repo.save(new AirControlArea(
                "LIS_FIR",
                new AirControlAreaBoundary(36.0, 42.0, -10.0, -6.0),
                "Lisbon FIR",
                1000.0
        ));
        repo.save(new AirControlArea(
                "MAD_FIR",
                new AirControlAreaBoundary(35.0, 43.0, -5.0, 0.0),
                "Madrid FIR",
                1200.0
        ));

        int count = 0;
        for (AirControlArea ignored : repo.findAll()) {
            count++;
        }

        assertEquals(2, count);
    }

    @Test
    public void ensureFindAllIsEmptyOnEmptyRepository() {
        assertFalse(repo.findAll().iterator().hasNext());
    }
}