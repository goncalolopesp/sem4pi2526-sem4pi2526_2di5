package pt.isep.sem4pi.application.controller.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.validations.Preconditions;
import pt.isep.sem4pi.application.service.usermanagment.UserAccessService;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;

/**
 * Controller da US030.
 *
 * Coordena autenticação, logout e alteração de password.
 * A UI apenas recolhe dados e apresenta resultados.
 */
public class AuthenticationController {

    public enum LoginResult {
        SUCCESS,
        INVALID_CREDENTIALS,
        ACCESS_DENIED
    }

    public enum ChangePasswordResult {
        SUCCESS,
        INVALID_AUTHENTICATION,
        PASSWORD_POLICY_VIOLATION
    }

    private final AuthenticationService authenticationService;
    private final AuthorizationService authorizationService;
    private final UserAccessService userAccessService;
    private final SEM4PIPasswordPolicy passwordPolicy;

    public AuthenticationController() {
        this.authenticationService = AuthzRegistry.authenticationService();
        this.authorizationService = AuthzRegistry.authorizationService();
        this.userAccessService = new UserAccessService();
        this.passwordPolicy = new SEM4PIPasswordPolicy();
    }

    public LoginResult login(final String username, final String password, final Role... requiredRoles) {
        Preconditions.nonEmpty(username, "Username must not be empty");
        Preconditions.nonEmpty(password, "Password must not be empty");
        Preconditions.nonNull(authenticationService, "Authentication service must be configured");
        Preconditions.nonNull(authorizationService, "Authorization service must be configured");

        final var session = authenticationService.authenticate(username.trim(), password, requiredRoles);

        if (session.isEmpty()) {
            return LoginResult.INVALID_CREDENTIALS;
        }

        if (!userAccessService.canUserAccess(username.trim())) {
            authorizationService.clearSession();
            return LoginResult.ACCESS_DENIED;
        }

        return LoginResult.SUCCESS;
    }

    public void logout() {
        Preconditions.nonNull(authorizationService, "Authorization service must be configured");

        if (authorizationService.hasSession()) {
            authorizationService.clearSession();
        }
    }

    public ChangePasswordResult changePassword(final String oldPassword, final String newPassword) {
        Preconditions.nonEmpty(oldPassword, "Old password must not be empty");
        Preconditions.nonEmpty(newPassword, "New password must not be empty");
        Preconditions.nonNull(authenticationService, "Authentication service must be configured");
        Preconditions.nonNull(authorizationService, "Authorization service must be configured");

        if (!authorizationService.hasSession()) {
            throw new IllegalStateException("An authenticated session is required");
        }

        if (!passwordPolicy.isSatisfiedBy(newPassword)) {
            return ChangePasswordResult.PASSWORD_POLICY_VIOLATION;
        }

        if (authenticationService.changePassword(oldPassword, newPassword)) {
            return ChangePasswordResult.SUCCESS;
        }

        return ChangePasswordResult.INVALID_AUTHENTICATION;
    }
}