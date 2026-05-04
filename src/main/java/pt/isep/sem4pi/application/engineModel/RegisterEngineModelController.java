package pt.isep.sem4pi.application.engineModel;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.math.BigDecimal;

public class RegisterEngineModelController {
    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final EngineModelRegistrationService registrationService;

    public RegisterEngineModelController() {
        this.registrationService = new EngineModelRegistrationService(
                PersistenceContext.repositories().engineModels(),
                PersistenceContext.repositories().manufacturers()
        );
    }

    public EngineModel registerEngineModel(final String engineModelCode, final String manufacturerCode,
                                           final String name, final String type, final BigDecimal power,
                                           final String fuelType, final BigDecimal efficiency) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return registrationService.register(engineModelCode, manufacturerCode, name, type, power, fuelType, efficiency);
    }
}
