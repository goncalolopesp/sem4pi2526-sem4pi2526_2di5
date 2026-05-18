package pt.isep.sem4pi.application.controller.engineModel;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.engineModel.EngineModelRegistrationService;
import pt.isep.sem4pi.domain.model.manufacture.Manufacturer;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RegisterEngineModelController {
    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final EngineModelRegistrationService registrationService;
    private final ManufacturerRepository manufacturerRepository;

    public RegisterEngineModelController() {
        this.manufacturerRepository = PersistenceContext.repositories().manufacturers();
        this.registrationService = new EngineModelRegistrationService(
                PersistenceContext.repositories().engineModels(),
                manufacturerRepository
        );
    }

    /**
     * Orquestra o registo e valida permissao de Backoffice/Admin.
     */
    public EngineModel registerEngineModel(final String engineModelCode, final String manufacturerCode,
                                           final String name, final EngineType type, final BigDecimal power,
                                           final FuelType fuelType, final BigDecimal efficiency) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return registrationService.register(engineModelCode, manufacturerCode, name, type, power, fuelType, efficiency);
    }

    /**
     * Lista fabricantes para apresentacao na UI.
     */
    public List<Manufacturer> listManufacturers() {
        final List<Manufacturer> manufacturers = new ArrayList<>();
        for (final Manufacturer manufacturer : manufacturerRepository.findAll()) {
            manufacturers.add(manufacturer);
        }
        return manufacturers;
    }
}
