package pt.isep.sem4pi.presentation.network.remoteaccess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo de visualizacao em memoria, thread-safe, usado pela US091.
 *
 * <p>Mantem um historico limitado de eventos do mais recente para o mais antigo
 * e deriva sessoes ativas a partir dos eventos de log da US090. Os metodos
 * sincronizados sao necessarios porque a ingestao UDP e as threads de pedidos
 * HTTP acedem ao mesmo estado em simultaneo.</p>
 */
public final class RemoteAccessLogStore {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int maximumEvents;
    private final Deque<LogEntry> events = new ArrayDeque<>();
    private final Map<SessionKey, ActiveUser> activeUsers = new LinkedHashMap<>();

    public RemoteAccessLogStore(final int maximumEvents) {
        if (maximumEvents <= 0) {
            throw new IllegalArgumentException("maximumEvents must be positive");
        }
        this.maximumEvents = maximumEvents;
    }

    /**
     * Reproduz um log persistido para que a visualizacao HTTP sobreviva a
     * reinicios do servidor.
     *
     * @param logFile log da US090 separado por ponto e virgula
     * @throws IOException quando um ficheiro existente nao pode ser lido
     */
    public synchronized void load(final Path logFile) throws IOException {
        if (!Files.exists(logFile)) {
            return;
        }
        for (final String line : Files.readAllLines(logFile, StandardCharsets.UTF_8)) {
            record(line);
        }
    }

    /**
     * Analisa e aplica um evento de log.
     *
     * <p>{@code LOGIN_SUCCESS} cria/substitui uma sessao, enquanto
     * {@code LOGOUT} e {@code DISCONNECT} removem a sessao correspondente.
     * Eventos de operacao de negocio e logins falhados continuam visiveis, mas
     * nao alteram o estado dos utilizadores ativos.</p>
     *
     * @param line evento da US090 separado por ponto e virgula
     * @return {@code false} quando a linha esta mal formada e foi ignorada
     */
    public synchronized boolean record(final String line) {
        final LogEntry entry = LogEntry.parse(line);
        if (entry == null) {
            return false;
        }

        events.addFirst(entry);
        while (events.size() > maximumEvents) {
            events.removeLast();
        }

        final SessionKey key = new SessionKey(
                entry.username(), entry.clientIp(), entry.clientPort(), entry.service());
        if ("LOGIN_SUCCESS".equals(entry.eventType())) {
            activeUsers.put(key, new ActiveUser(
                    entry.username(), entry.clientIp(), entry.clientPort(),
                    entry.service(), entry.timestamp()));
        } else if ("LOGOUT".equals(entry.eventType()) || "DISCONNECT".equals(entry.eventType())) {
            activeUsers.remove(key);
        }
        return true;
    }

    /**
     * @return copia defensiva do historico visivel, do mais recente para o mais antigo
     */
    public synchronized List<LogEntry> recentEvents() {
        return new ArrayList<>(events);
    }

    /**
     * @return copia defensiva das sessoes atualmente consideradas ativas
     */
    public synchronized List<ActiveUser> activeUsers() {
        return new ArrayList<>(activeUsers.values());
    }

    /**
     * Representacao analisada de um evento persistido de acesso remoto.
     */
    public record LogEntry(
            LocalDateTime timestamp,
            String username,
            String clientIp,
            int clientPort,
            String service,
            String eventType) {

        private static LogEntry parse(final String line) {
            if (line == null || line.isBlank()) {
                return null;
            }
            final String[] fields = line.trim().split(";", -1);
            if (fields.length != 6) {
                return null;
            }
            try {
                return new LogEntry(
                        LocalDateTime.parse(fields[0], TIMESTAMP_FORMAT),
                        fields[1],
                        fields[2],
                        Integer.parseInt(fields[3]),
                        fields[4],
                        fields[5]);
            } catch (final DateTimeParseException | NumberFormatException ex) {
                return null;
            }
        }

        public String formattedTimestamp() {
            return timestamp.format(TIMESTAMP_FORMAT);
        }
    }

    /**
     * Snapshot de uma sessao aberta por um login bem-sucedido.
     */
    public record ActiveUser(
            String username,
            String clientIp,
            int clientPort,
            String service,
            LocalDateTime connectedSince) {

        public String formattedConnectedSince() {
            return connectedSince.format(TIMESTAMP_FORMAT);
        }
    }

    private record SessionKey(String username, String clientIp, int clientPort, String service) {
    }
}
