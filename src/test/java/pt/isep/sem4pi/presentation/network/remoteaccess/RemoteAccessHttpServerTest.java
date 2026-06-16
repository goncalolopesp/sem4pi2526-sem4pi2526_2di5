package pt.isep.sem4pi.presentation.network.remoteaccess;

import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testes dos endpoints HTTP da US091.
 *
 * <p>O servidor expoe paginas de browser e endpoints JSON usados por polling
 * AJAX, sem adicionar uma framework web externa.</p>
 */
public class RemoteAccessHttpServerTest {

    /**
     * Verificacao HTTP end-to-end: as paginas incluem JavaScript de
     * auto-refresh, os endpoints JSON expoem dados vivos do store e um
     * disconnect altera imediatamente a resposta de utilizadores ativos.
     */
    @Test
    public void servesPagesAndLiveJsonEndpoints() throws Exception {
        final RemoteAccessLogStore store = new RemoteAccessLogStore(10);
        store.record("2026-06-12T10:00:00;ana;127.0.0.1;5001;US44;LOGIN_SUCCESS");
        final RemoteAccessHttpServer server = new RemoteAccessHttpServer(0, store);
        server.start();

        try {
            final String baseUrl = "http://127.0.0.1:" + server.port();
            final HttpClient client = HttpClient.newHttpClient();

            final HttpResponse<String> eventsPage = get(client, baseUrl + "/events");
            assertEquals(200, eventsPage.statusCode());
            assertTrue(eventsPage.body().contains("fetch('/api/events'"));
            assertTrue(eventsPage.body().contains("setInterval(refresh,2000)"));
            assertTrue(eventsPage.body().contains("rows.removeChild(rows.firstChild)"));

            final HttpResponse<String> usersPage = get(client, baseUrl + "/active-users");
            assertEquals(200, usersPage.statusCode());
            assertTrue(usersPage.body().contains("fetch('/api/active-users'"));

            final HttpResponse<String> events = get(client, baseUrl + "/api/events");
            assertEquals("application/json; charset=utf-8",
                    events.headers().firstValue("Content-Type").orElse(""));
            assertTrue(events.body().contains("\"eventType\":\"LOGIN_SUCCESS\""));

            final HttpResponse<String> users = get(client, baseUrl + "/api/active-users");
            assertTrue(users.body().contains("\"username\":\"ana\""));

            store.record("2026-06-12T10:01:00;ana;127.0.0.1;5001;US44;DISCONNECT");
            final HttpResponse<String> updatedUsers = get(client, baseUrl + "/api/active-users");
            assertEquals("[]", updatedUsers.body());
        } finally {
            server.stop();
        }
    }

    private static HttpResponse<String> get(final HttpClient client, final String url) throws Exception {
        return client.send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
