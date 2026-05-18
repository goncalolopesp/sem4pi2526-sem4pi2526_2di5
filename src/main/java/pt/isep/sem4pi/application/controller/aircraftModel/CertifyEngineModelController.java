package pt.isep.sem4pi.application.controller.aircraftModel;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.aircraftModel.AircraftModelCertificationService;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;

public class CertifyEngineModelController {
    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AircraftModelRepository aircraftModelRepository;
    private final EngineModelRepository engineModelRepository;
    private final AircraftModelCertificationService certificationService;

    public CertifyEngineModelController() {
        this.aircraftModelRepository = PersistenceContext.repositories().aircraftModels();
        this.engineModelRepository = PersistenceContext.repositories().engineModels();
        this.certificationService = new AircraftModelCertificationService(aircraftModelRepository, engineModelRepository);
    }

    /**
     * Garante permissao e delega a certificacao para o servico.
     */
    public AircraftModel certifyEngineModel(final AircraftModelId aircraftModelId, final EngineModelId engineModelId) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return certificationService.certifyEngine(aircraftModelId, engineModelId);
    }

    /**
     * Lista modelos de aeronave para selecao na UI.
     */
    public List<AircraftModel> listAircraftModels() {
        final List<AircraftModel> models = new ArrayList<>();
        for (final AircraftModel model : aircraftModelRepository.findAll()) {
            models.add(model);
        }
        return models;
    }

    /**
     * Lista modelos de motor para selecao na UI.
     */
    public List<EngineModel> listEngineModels() {
        final List<EngineModel> models = new ArrayList<>();
        for (final EngineModel model : engineModelRepository.findAll()) {
            models.add(model);
        }
        return models;
    }
}
