package pt.isep.sem4pi.presentation.network.remoteaccess;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP embebido que implementa a visualizacao em browser da US091.
 *
 * <p>As paginas HTML contem um pequeno cliente JavaScript que consulta
 * endpoints JSON a cada dois segundos. O browser substitui apenas as linhas da
 * tabela, por isso eventos e utilizadores ativos atualizam sem recarregar a
 * pagina inteira. O escaping JSON no servidor e o {@code textContent} no DOM
 * impedem que valores do log sejam interpretados como HTML.</p>
 */
public final class RemoteAccessHttpServer {

    private final HttpServer server;
    private final RemoteAccessLogStore store;

    /**
     * Cria todas as rotas de paginas e API da US091.
     *
     * @param port porta HTTP pedida; zero permite ao sistema operativo escolher
     * @param store visualizacao viva partilhada de eventos e sessoes ativas
     * @throws IOException quando o socket HTTP nao pode ser criado
     */
    public RemoteAccessHttpServer(final int port, final RemoteAccessLogStore store) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.store = store;
        server.createContext("/", this::serveHome);
        server.createContext("/events", exchange -> servePage(exchange, "Last Recorded Events", "events"));
        server.createContext("/active-users", exchange -> servePage(exchange, "Currently Active Users", "active-users"));
        server.createContext("/api/events", this::serveEvents);
        server.createContext("/api/active-users", this::serveActiveUsers);
        server.setExecutor(Executors.newCachedThreadPool());
    }

    /**
     * Comeca a aceitar pedidos do browser.
     */
    public void start() {
        server.start();
    }

    /**
     * Para imediatamente o servidor embebido.
     */
    public void stop() {
        server.stop(0);
    }

    /**
     * @return porta real atribuida, util quando os testes pedem uma porta efemera
     */
    public int port() {
        return server.getAddress().getPort();
    }

    private void serveHome(final HttpExchange exchange) throws IOException {
        if (!isGet(exchange)) {
            return;
        }
        final String body = """
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Remote Access Logs</title><style>%s</style></head>
                <body><main><h1>Remote Access Logging Server</h1>
                <p>Select a live status page.</p>
                <nav><a href="/events">Last recorded events</a>
                <a href="/active-users">Currently active users</a></nav></main></body></html>
                """.formatted(css());
        send(exchange, 200, "text/html; charset=utf-8", body);
    }

    private void servePage(final HttpExchange exchange, final String title, final String page) throws IOException {
        if (!isGet(exchange)) {
            return;
        }
        final String headings = "events".equals(page)
                ? "<th>Timestamp</th><th>User</th><th>Client</th><th>Service</th><th>Event</th>"
                : "<th>User</th><th>Client</th><th>Service</th><th>Connected since</th>";
        final String body = """
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>%s</title><style>%s</style></head><body><main>
                <nav><a href="/">Home</a><a href="/events">Events</a>
                <a href="/active-users">Active users</a></nav>
                <h1>%s</h1><p id="status">Loading...</p>
                <div class="table-wrap"><table><thead><tr>%s</tr></thead>
                <tbody id="rows"></tbody></table></div>
                </main><script>%s</script></body></html>
                """.formatted(title, css(), title, headings, javascript(page));
        send(exchange, 200, "text/html; charset=utf-8", body);
    }

    private void serveEvents(final HttpExchange exchange) throws IOException {
        if (!isGet(exchange)) {
            return;
        }
        final List<RemoteAccessLogStore.LogEntry> events = store.recentEvents();
        final StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < events.size(); i++) {
            final var event = events.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"timestamp\":\"").append(escapeJson(event.formattedTimestamp()))
                    .append("\",\"username\":\"").append(escapeJson(event.username()))
                    .append("\",\"clientIp\":\"").append(escapeJson(event.clientIp()))
                    .append("\",\"clientPort\":").append(event.clientPort())
                    .append(",\"service\":\"").append(escapeJson(event.service()))
                    .append("\",\"eventType\":\"").append(escapeJson(event.eventType()))
                    .append("\"}");
        }
        json.append(']');
        send(exchange, 200, "application/json; charset=utf-8", json.toString());
    }

    private void serveActiveUsers(final HttpExchange exchange) throws IOException {
        if (!isGet(exchange)) {
            return;
        }
        final List<RemoteAccessLogStore.ActiveUser> users = store.activeUsers();
        final StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            final var user = users.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"username\":\"").append(escapeJson(user.username()))
                    .append("\",\"clientIp\":\"").append(escapeJson(user.clientIp()))
                    .append("\",\"clientPort\":").append(user.clientPort())
                    .append(",\"service\":\"").append(escapeJson(user.service()))
                    .append("\",\"connectedSince\":\"").append(escapeJson(user.formattedConnectedSince()))
                    .append("\"}");
        }
        json.append(']');
        send(exchange, 200, "application/json; charset=utf-8", json.toString());
    }

    private static boolean isGet(final HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            return true;
        }
        exchange.getResponseHeaders().set("Allow", "GET");
        send(exchange, 405, "text/plain; charset=utf-8", "Method Not Allowed");
        return false;
    }

    private static void send(
            final HttpExchange exchange, final int status, final String contentType, final String body)
            throws IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String escapeJson(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String javascript(final String page) {
        final boolean events = "events".equals(page);
        final String endpoint = events ? "/api/events" : "/api/active-users";
        final String cells = events
                ? "[item.timestamp,item.username,item.clientIp+':'+item.clientPort,item.service,item.eventType]"
                : "[item.username,item.clientIp+':'+item.clientPort,item.service,item.connectedSince]";
        return """
                const rows=document.getElementById('rows'),status=document.getElementById('status');
                function cell(value){const td=document.createElement('td');td.textContent=value;return td;}
                async function refresh(){
                  try{
                    const response=await fetch('%s',{cache:'no-store'});
                    if(!response.ok)throw new Error('HTTP '+response.status);
                    const data=await response.json();
                    while(rows.firstChild)rows.removeChild(rows.firstChild);
                    for(const item of data){const tr=document.createElement('tr');
                      for(const value of %s)tr.appendChild(cell(value));rows.appendChild(tr);}
                    status.textContent=data.length+' record(s) - updated '+new Date().toLocaleTimeString();
                  }catch(error){status.textContent='Update failed: '+error.message;}
                }
                refresh();setInterval(refresh,2000);
                """.formatted(endpoint, cells);
    }

    private static String css() {
        return """
                :root{font-family:system-ui,sans-serif;color:#172033;background:#f3f6fb}
                body{margin:0}main{max-width:1100px;margin:auto;padding:2rem}
                nav{display:flex;gap:1rem;flex-wrap:wrap;margin-bottom:2rem}
                a{color:#0757a8;font-weight:650;text-decoration:none;margin-right:1rem}
                h1{margin-bottom:.4rem}.table-wrap{overflow-x:auto;background:white;
                border-radius:.6rem;box-shadow:0 2px 12px #17203318}
                table{border-collapse:collapse;width:100%}th,td{padding:.8rem 1rem;
                text-align:left;border-bottom:1px solid #dde3ec}th{background:#e8eef7}
                tbody tr:hover{background:#f7f9fc}#status{color:#58657a}
                """;
    }
}
