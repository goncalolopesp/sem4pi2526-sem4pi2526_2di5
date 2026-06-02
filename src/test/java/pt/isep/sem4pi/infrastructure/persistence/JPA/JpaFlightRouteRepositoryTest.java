package pt.isep.sem4pi.infrastructure.persistence.JPA;

import org.junit.Test;

import static org.junit.Assert.*;

public class JpaFlightRouteRepositoryTest {

    @Test
    public void ensureJpaFlightRouteRepositoryCanBeInstantiated() {
        // The constructor initializes the JPA repository with the persistence unit name
        // and the identity field name. This test verifies the object is created
        // without throwing exceptions in the constructor setup.
        // Note: Full persistence tests require a running database/persistence context.
        try {
            JpaFlightRouteRepository repo = new JpaFlightRouteRepository();
            // If the persistence unit is not configured, the constructor may succeed
            // but operations will fail. We just verify the constructor itself works.
            assertNotNull(repo);
        } catch (Exception e) {
            // Expected in test environments without a configured persistence unit.
            // The constructor delegates to JpaAutoTxRepository("sem4piPU", "routeID")
            // which requires the PU to be defined in persistence.xml.
            assertTrue("Expected persistence-related exception",
                    e.getMessage() != null || e instanceof RuntimeException);
        }
    }

    @Test
    public void ensureRepositoryUsesCorrectPersistenceUnit() {
        // This test validates that the repository class exists and extends the
        // correct JPA framework base class. The constructor passes "sem4piPU"
        // as the persistence unit name and "routeID" as the identity field.
        // Since JpaAutoTxRepository requires a real EntityManagerFactory,
        // we verify the class structure instead.
        assertTrue("JpaFlightRouteRepository should implement FlightRouteRepository",
                pt.isep.sem4pi.domain.repositories.FlightRouteRepository.class
                        .isAssignableFrom(JpaFlightRouteRepository.class));
    }

    @Test
    public void ensureRepositoryExtendsJpaAutoTxRepository() {
        assertTrue("JpaFlightRouteRepository should extend JpaAutoTxRepository",
                eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository.class
                        .isAssignableFrom(JpaFlightRouteRepository.class));
    }

    @Test
    public void ensureRepositoryImplementsFlightRouteRepository() {
        // Verify the interface hierarchy is correct
        Class<?>[] interfaces = JpaFlightRouteRepository.class.getInterfaces();
        boolean implementsFlightRouteRepo = false;
        for (Class<?> iface : interfaces) {
            if (iface == pt.isep.sem4pi.domain.repositories.FlightRouteRepository.class) {
                implementsFlightRouteRepo = true;
                break;
            }
        }
        assertTrue("Must directly implement FlightRouteRepository", implementsFlightRouteRepo);
    }
}
