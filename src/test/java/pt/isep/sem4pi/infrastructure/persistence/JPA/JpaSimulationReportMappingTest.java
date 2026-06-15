package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.simulation.FlightExecutionSummary;
import pt.isep.sem4pi.domain.model.simulation.ReportFilePath;
import pt.isep.sem4pi.domain.model.simulation.SafetyViolationSummary;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.SimulationValidationResult;

import java.time.LocalDateTime;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JpaSimulationReportMappingTest {

    @Test
    public void persistsAndLoadsCompleteSimulationReportAggregate() {
        // DEFESA:
        // Este teste de integração JPA valida o mapping do Aggregate Root e das coleções de Value Objects.
        final Map<String, String> properties = Map.of(
                "jakarta.persistence.jdbc.url",
                "jdbc:h2:mem:us111-mapping;DB_CLOSE_DELAY=-1",
                "hibernate.hbm2ddl.auto", "create-drop");

        try (EntityManagerFactory factory =
                     Persistence.createEntityManagerFactory("sem4piPU", properties)) {
            persistReport(factory);

            try (EntityManager entityManager = factory.createEntityManager()) {
                final SimulationReport loaded =
                        entityManager.find(SimulationReport.class, "REPORT-1");

                assertEquals("SIM-1", loaded.simulationId());
                assertEquals(Integer.valueOf(2), loaded.totalFlights());
                assertEquals("FAIL", loaded.finalValidationResult());
                assertEquals(2, loaded.flightSummaries().size());
                assertEquals(1, loaded.safetyViolations().size());
                assertEquals(
                        Path.of("reports", "simulation", "SIM-1.txt").toString(),
                        loaded.fileLocation());
            }
        }
    }

    private void persistReport(final EntityManagerFactory factory) {
        final SimulationReport report = new SimulationReport(
                "REPORT-1",
                "SIM-1",
                LocalDateTime.of(2026, 6, 11, 10, 15),
                new ReportFilePath("reports/simulation/SIM-1.txt"),
                List.of(
                        new FlightExecutionSummary("FLIGHT-A", "FINISHED"),
                        new FlightExecutionSummary("FLIGHT-B", "FINISHED")),
                List.of(new SafetyViolationSummary(
                        7,
                        "SEPARATION",
                        "FLIGHT-A: lat=41.1; FLIGHT-B: lat=40.9",
                        "distance below minimum")),
                SimulationValidationResult.PASS);

        try (EntityManager entityManager = factory.createEntityManager()) {
            entityManager.getTransaction().begin();
            entityManager.persist(report);
            entityManager.getTransaction().commit();
        }
    }
}
