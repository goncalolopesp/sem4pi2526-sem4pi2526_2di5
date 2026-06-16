package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.flightplan.AssociateWeatherDataService;
import pt.isep.sem4pi.application.service.flightplan.WeatherAssociationResult;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller aplicacional da US082, associacao de dados meteorologicos existentes a um plano de voo.
 *
 * <p>Obtem o username autenticado e se o utilizador e administrador, passando
 * esses dados ao servico para validacao de propriedade. Pilotos apenas podem
 * gerir os seus planos; administradores podem gerir todos os planos.</p>
 */
@UseCaseController
public class AssociateWeatherDataController {

    private final AuthorizationService authz;
    private final AssociateWeatherDataService service;

    public AssociateWeatherDataController() {
        this.authz = AuthzRegistry.authorizationService();
        final var repositories = PersistenceContext.repositories();
        this.service = new AssociateWeatherDataService(
                repositories.flightPlans(), repositories.weatherBatches());
    }

    public AssociateWeatherDataController(AuthorizationService authz,
                                          AssociateWeatherDataService service) {
        this.authz = authz;
        this.service = service;
    }

    /**
     * Lista os planos de voo visiveis para o piloto ou administrador autenticado.
     *
     * @return planos do piloto, ou todos os planos para um administrador
     */
    public List<FlightPlan> myFlightPlans() {
        ensureAuthorized();
        return service.listFlightPlansAccessibleBy(currentUsername(), isAdmin());
    }

    /**
     * Lista registos meteorologicos persistidos disponiveis para selecao.
     *
     * @return DTOs de visualizacao ordenados com os identificadores necessarios para associacao
     */
    public List<WeatherDataViewDTO> weatherData() {
        ensureAuthorized();
        return service.listWeatherData();
    }

    public WeatherAssociationResult associate(String flightPlanId, String areaId, LocalDate date,
                                              LocalTime startTime, LocalTime endTime) {
        ensureAuthorized();
        return service.associate(
                flightPlanId, areaId, date, startTime, endTime, currentUsername(), isAdmin());
    }

    /**
     * Associa varios registos meteorologicos selecionados numa unica operacao.
     *
     * @param flightPlanId identidade do plano de voo alvo
     * @param weatherDataIds identidades persistidas dos dados meteorologicos
     * @return plano atualizado, numero de adicoes e estado de invalidacao do teste
     */
    public WeatherAssociationResult associate(String flightPlanId, List<Long> weatherDataIds) {
        ensureAuthorized();
        return service.associateByIds(
                flightPlanId, weatherDataIds, currentUsername(), isAdmin());
    }

    private void ensureAuthorized() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
    }

    private boolean isAdmin() {
        return authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN);
    }

    private String currentUsername() {
        return authz.session()
                .orElseThrow(() -> new IllegalStateException("An authenticated session is required."))
                .authenticatedUser().identity().toString();
    }
}
