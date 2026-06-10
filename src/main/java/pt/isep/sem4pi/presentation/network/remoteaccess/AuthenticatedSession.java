package pt.isep.sem4pi.presentation.network.remoteaccess;

import java.util.concurrent.Callable;

public interface AuthenticatedSession {

    String username();

    String password();

    boolean authenticated();

    void ensureAuthenticated();

    <T> T runAuthenticated(Callable<T> action);
}
