package pt.isep.sem4pi.presentation.network.remoteaccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class RemoteAccessServiceRegistry {

    private RemoteAccessServiceRegistry() {
        // utility
    }

    public static List<RemoteAccessService> availableServices(final Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        final EnumSet<RemoteAccessService> services = EnumSet.noneOf(RemoteAccessService.class);
        if (hasAnyRole(roles, "ADMIN", "WEATHER_PERSON", "PILOT", "FLIGHT_CONTROL_OPERATOR")) {
            services.add(RemoteAccessService.WEATHER);
        }
        if (hasAnyRole(roles, "ADMIN", "PILOT")) {
            services.add(RemoteAccessService.PILOT);
        }
        if (hasAnyRole(roles, "ADMIN", "AIR_TRANSPORT_COMPANY_COLLABORATOR")) {
            services.add(RemoteAccessService.ATCC);
        }
        final List<RemoteAccessService> ordered = new ArrayList<>(services);
        return Collections.unmodifiableList(ordered);
    }

    public static String formatMenu(final List<RemoteAccessService> services) {
        if (services.isEmpty()) {
            return "-";
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < services.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(services.get(i).id());
        }
        return builder.toString();
    }

    private static boolean hasAnyRole(final Set<String> roles, final String... expected) {
        for (final String role : expected) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
