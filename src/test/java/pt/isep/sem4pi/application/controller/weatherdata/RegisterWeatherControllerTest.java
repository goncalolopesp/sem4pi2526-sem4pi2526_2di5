package pt.isep.sem4pi.application.controller.weatherdata;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryWeatherBatchRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RegisterWeatherControllerTest {

    @Test
    public void ensureRegisterWeatherPersistsBatch() {
        InMemoryWeatherBatchRepository repository = new InMemoryWeatherBatchRepository();
        AuthorizationService authz = mock(AuthorizationService.class);
        RegisterWeatherController controller = new RegisterWeatherController(repository, authz);

        WeatherImportBatch batch = controller.registerWeather("121", 20.0, 10.0, 90.0, 50.0, 1013.0, 150.0);

        assertNotNull(batch);
        assertEquals(1, repository.findAllByAreaId("121").size());
        verify(authz).ensureAuthenticatedUserHasAnyOf(
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.WEATHER_PERSON
        );
    }
}

