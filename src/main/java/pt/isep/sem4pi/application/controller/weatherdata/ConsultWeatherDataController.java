package pt.isep.sem4pi.application.controller.weatherdata;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.weatherdata.ConsultWeatherDataService;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller aplicacional da US043, consulta de dados meteorologicos por area e dia.
 *
 * <p>Garante os quatro papeis permitidos pelo requisito e delega a consulta
 * num servico para que a camada de apresentacao nunca aceda diretamente aos
 * repositorios.</p>
 */
@UseCaseController
public class ConsultWeatherDataController {

    private final AuthorizationService authz;
    private final ConsultWeatherDataService service;

    public ConsultWeatherDataController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().weatherBatches());
    }

    public ConsultWeatherDataController(AuthorizationService authz, WeatherBatchRepository repository) {
        this.authz = authz;
        this.service = new ConsultWeatherDataService(repository);
    }

    /**
     * Devolve medicoes meteorologicas de uma Air Control Area num dia especifico.
     *
     * @param areaId identificador da Air Control Area
     * @param date dia pedido
     * @return registos meteorologicos encontrados, ou lista vazia quando nao existem
     */
    public List<WeatherDataViewDTO> consultByAreaAndDate(String areaId, LocalDate date) {
        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.ADMIN,
                SEM4PIRoles.WEATHER_PERSON,
                SEM4PIRoles.PILOT,
                SEM4PIRoles.FLIGHT_CONTROL_OPERATOR
        );
        return service.findByAreaAndDate(areaId, date);
    }
}

