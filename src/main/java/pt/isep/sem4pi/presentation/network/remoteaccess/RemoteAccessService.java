package pt.isep.sem4pi.presentation.network.remoteaccess;

public enum RemoteAccessService {

    WEATHER("WEATHER", "Weather Data", "US44");

    private final String id;
    private final String label;
    private final String userStory;

    RemoteAccessService(final String id, final String label, final String userStory) {
        this.id = id;
        this.label = label;
        this.userStory = userStory;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public String userStory() {
        return userStory;
    }

    public static RemoteAccessService fromId(final String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        final String normalized = raw.trim().toUpperCase();
        for (final RemoteAccessService service : values()) {
            if (service.id.equals(normalized)) {
                return service;
            }
        }
        return null;
    }
}
