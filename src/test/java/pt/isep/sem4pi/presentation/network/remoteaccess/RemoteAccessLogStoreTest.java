package pt.isep.sem4pi.presentation.network.remoteaccess;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the in-memory state model used by US091.
 *
 * <p>The store is the bridge between the persisted US090 log file and the live
 * HTTP pages: it keeps recent events and reconstructs active sessions.</p>
 */
public class RemoteAccessLogStoreTest {

    /**
     * Recent events are shown newest first and capped so the browser page stays
     * bounded.
     */
    @Test
    public void keepsMostRecentEventsFirstAndLimitsTheirNumber() {
        final RemoteAccessLogStore store = new RemoteAccessLogStore(2);

        store.record("2026-06-12T10:00:00;ana;127.0.0.1;5001;US44;LOGIN_SUCCESS");
        store.record("2026-06-12T10:01:00;ana;127.0.0.1;5001;US44;CONSULT_WEATHER_DATA");
        store.record("2026-06-12T10:02:00;ana;127.0.0.1;5001;US44;LOGOUT");

        assertEquals(2, store.recentEvents().size());
        assertEquals("LOGOUT", store.recentEvents().get(0).eventType());
        assertEquals("CONSULT_WEATHER_DATA", store.recentEvents().get(1).eventType());
    }

    /**
     * Active users are derived from login/logout events rather than stored as a
     * separate persistent entity.
     */
    @Test
    public void loginAddsAndLogoutRemovesActiveSession() {
        final RemoteAccessLogStore store = new RemoteAccessLogStore(10);

        store.record("2026-06-12T10:00:00;ana;127.0.0.1;5001;US44;LOGIN_SUCCESS");
        assertEquals(1, store.activeUsers().size());
        assertEquals("ana", store.activeUsers().get(0).username());

        store.record("2026-06-12T10:02:00;ana;127.0.0.1;5001;US44;LOGOUT");
        assertTrue(store.activeUsers().isEmpty());
    }

    /**
     * Startup replay: existing log files rebuild the visible state, while
     * malformed lines are ignored.
     */
    @Test
    public void loadsExistingLogAndIgnoresInvalidLines() throws Exception {
        final Path file = Files.createTempFile("remote-access-store-", ".log");
        Files.writeString(file, """
                invalid line
                2026-06-12T10:00:00;bruno;10.0.0.2;5002;US44;LOGIN_SUCCESS
                """);
        final RemoteAccessLogStore store = new RemoteAccessLogStore(10);

        store.load(file);

        assertEquals(1, store.recentEvents().size());
        assertEquals(1, store.activeUsers().size());
        assertFalse(store.record("also invalid"));
    }
}
