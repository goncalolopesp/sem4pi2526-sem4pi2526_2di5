package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.util.Utility;

/**
 * Helper to build SystemUser and SignupRequest objects using the EAPLI framework builders.
 */
@Utility
public final class UserBuilderHelper {

    private UserBuilderHelper() {
        // utility
    }

    public static SystemUserBuilder builder() {
        return new SystemUserBuilder(new SEM4PIPasswordPolicy(), new PlainTextEncoder());
    }
}

