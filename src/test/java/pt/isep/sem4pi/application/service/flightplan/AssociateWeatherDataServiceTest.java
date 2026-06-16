package pt.isep.sem4pi.application.service.flightplan;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.repositories.FlightPlanRepository;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Testes ao nivel do servico para a US082, adicionar dados meteorologicos a planos de voo.
 *
 * <p>O servico possui a parte cross-aggregate do caso de uso: resolve planos
 * de voo e dados meteorologicos, valida a propriedade do piloto, permite o
 * bypass de admin atraves da flag {@code canManageAll} e persiste o plano
 * alterado.</p>
 */
public class AssociateWeatherDataServiceTest {

    private static final String PLAN_ID = "FP-001";
    private static final String AREA_ID = "LPPC";
    private static final LocalDate DATE = LocalDate.of(2026, 6, 9);
    private static final LocalTime START = LocalTime.of(10, 0);
    private static final LocalTime END = LocalTime.of(11, 0);

    private FlightPlanRepository flightPlanRepository;
    private WeatherBatchRepository weatherRepository;
    private AssociateWeatherDataService service;
    private FlightPlan plan;
    private Pilot pilot;

    @Before
    public void setUp() {
        flightPlanRepository = mock(FlightPlanRepository.class);
        weatherRepository = mock(WeatherBatchRepository.class);
        service = new AssociateWeatherDataService(flightPlanRepository, weatherRepository);
        plan = mock(FlightPlan.class);
        pilot = mock(Pilot.class);
        when(plan.pilot()).thenReturn(pilot);
    }

    /**
     * Fluxo antigo de um unico registo: meteorologia existente e associada e o
     * resultado indica a UI quando um teste anterior ficou invalido.
     */
    @Test
    public void ensureAssociationPersistsExistingWeatherAndReportsInvalidation() {
        final WeatherData weatherData = mock(WeatherData.class);
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));
        when(weatherRepository.findWeatherData(AREA_ID, DATE, START, END))
                .thenReturn(Optional.of(weatherData));
        when(plan.associateWeatherData(weatherData)).thenReturn(true);
        when(flightPlanRepository.save(plan)).thenReturn(plan);

        final WeatherAssociationResult result =
                service.associate(PLAN_ID, AREA_ID, DATE, START, END, "pilot");

        assertSame(plan, result.flightPlan());
        assertTrue(result.previousTestInvalidated());
        verify(plan).associateWeatherData(weatherData);
        verify(flightPlanRepository).save(plan);
    }

    /**
     * Um piloto nao pode alterar o plano de voo de outro piloto; esta e a
     * protecao de propriedade exigida pela US082.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ensurePilotCannotUpdateAnotherPilotsFlightPlan() {
        when(pilot.isOwnedBy("other-pilot")).thenReturn(false);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));

        service.associate(PLAN_ID, AREA_ID, DATE, START, END, "other-pilot");
    }

    /**
     * A meteorologia ja deve estar persistida pela US041/US042 antes de poder
     * ser ligada a um plano de voo.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ensureWeatherMustAlreadyExist() {
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));
        when(weatherRepository.findWeatherData(AREA_ID, DATE, START, END))
                .thenReturn(Optional.empty());

        service.associate(PLAN_ID, AREA_ID, DATE, START, END, "pilot");
    }

    /**
     * A lista mostrada a um piloto contem apenas planos desse piloto.
     */
    @Test
    public void ensureOnlyOwnedFlightPlansAreListed() {
        final FlightPlan otherPlan = mock(FlightPlan.class);
        final Pilot otherPilot = mock(Pilot.class);
        when(otherPlan.pilot()).thenReturn(otherPilot);
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(otherPilot.isOwnedBy("pilot")).thenReturn(false);
        when(flightPlanRepository.findAll()).thenReturn(List.of(plan, otherPlan));

        final List<FlightPlan> result = service.listFlightPlansOwnedBy("pilot");

        assertEquals(List.of(plan), result);
    }

    /**
     * Cenario principal em lote da US082: varias linhas meteorologicas
     * selecionadas sao associadas numa operacao e o plano e guardado uma vez no fim.
     */
    @Test
    public void ensureSeveralWeatherRecordsCanBeAssociatedInOneOperation() {
        final WeatherData first = mock(WeatherData.class);
        final WeatherData second = mock(WeatherData.class);
        final List<WeatherData> associated = new java.util.ArrayList<>();
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));
        when(weatherRepository.findWeatherDataById(1L)).thenReturn(Optional.of(first));
        when(weatherRepository.findWeatherDataById(2L)).thenReturn(Optional.of(second));
        when(plan.weatherData()).thenAnswer(invocation -> List.copyOf(associated));
        when(plan.associateWeatherData(any())).thenAnswer(invocation -> {
            associated.add(invocation.getArgument(0));
            return associated.size() == 1;
        });
        when(flightPlanRepository.save(plan)).thenReturn(plan);

        final WeatherAssociationResult result =
                service.associateByIds(PLAN_ID, List.of(1L, 2L), "pilot", false);

        assertEquals(2, result.associatedWeatherCount());
        assertTrue(result.previousTestInvalidated());
        verify(plan).associateWeatherData(first);
        verify(plan).associateWeatherData(second);
        verify(flightPlanRepository, times(1)).save(plan);
    }

    /**
     * IDs selecionados repetidos sao deduplicados antes da associacao,
     * impedindo que o mesmo registo meteorologico seja processado varias vezes.
     */
    @Test
    public void ensureRepeatedWeatherIdsAreProcessedOnlyOnce() {
        final WeatherData weatherData = mock(WeatherData.class);
        final List<WeatherData> associated = new java.util.ArrayList<>();
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));
        when(weatherRepository.findWeatherDataById(1L)).thenReturn(Optional.of(weatherData));
        when(plan.weatherData()).thenAnswer(invocation -> List.copyOf(associated));
        when(plan.associateWeatherData(weatherData)).thenAnswer(invocation -> {
            associated.add(weatherData);
            return false;
        });
        when(flightPlanRepository.save(plan)).thenReturn(plan);

        final WeatherAssociationResult result =
                service.associateByIds(PLAN_ID, List.of(1L, 1L), "pilot", false);

        assertEquals(1, result.associatedWeatherCount());
        assertFalse(result.previousTestInvalidated());
        verify(plan, times(1)).associateWeatherData(weatherData);
    }
}
