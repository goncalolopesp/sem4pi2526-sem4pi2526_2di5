package pt.isep.sem4pi.presentation.network.remoteaccess;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testa o modelo de estado em memoria usado pela US091.
 *
 * <p>O store e a ponte entre o ficheiro de log persistido da US090 e as
 * paginas HTTP em direto: mantem eventos recentes e reconstrói sessoes ativas.</p>
 */
public class RemoteAccessLogStoreTest {

    /**
     * Eventos recentes sao mostrados do mais recente para o mais antigo e
     * limitados para manter a pagina do browser controlada.
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
     * Utilizadores ativos sao derivados de eventos de login/logout em vez de
     * serem guardados como entidade persistente separada.
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
     * Replay no arranque: ficheiros de log existentes reconstroem o estado
     * visivel, enquanto linhas mal formadas sao ignoradas.
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
