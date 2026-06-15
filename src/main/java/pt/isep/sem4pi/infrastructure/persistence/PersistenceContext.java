package pt.isep.sem4pi.infrastructure.persistence;

import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Ponto central para obter repositorios (in-memory ou JPA) via RepositoryFactory.
 *
 * Resolve a factory a partir de properties ou override por JVM property.
 */
public final class PersistenceContext {

    private static RepositoryFactory theFactory;

    private PersistenceContext() {
        // utility
    }

    /**
     * Devolve a factory de repositorios configurada para a aplicacao.
     */
    public static RepositoryFactory repositories() {
        if (theFactory == null) {
            theFactory = buildRepositoryFactory();
        }

        return theFactory;
    }

    public static void resetForTesting() {
        theFactory = null;
    }

    private static RepositoryFactory buildRepositoryFactory() {
        // 1) Allow override from JVM/system properties (useful for tests)
        final String overrideFactoryClassName = System.getProperty("persistence.repositoryFactory");
        if (overrideFactoryClassName != null && !overrideFactoryClassName.trim().isEmpty()) {
            try {
                final Class<?> factoryClass = Class.forName(overrideFactoryClassName.trim());
                return (RepositoryFactory) factoryClass.getDeclaredConstructor().newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to load repository factory from system property", e);
            }
        }

        // 2) application.properties (default)
        final Properties properties = new Properties();

        try (InputStream input = PersistenceContext.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input != null) {
                properties.load(input);
            }

            final String factoryClassName = properties.getProperty(
                    "persistence.repositoryFactory",
                    InMemoryRepositoryFactory.class.getName()
            );

            final Class<?> factoryClass = Class.forName(factoryClassName);
            return (RepositoryFactory) factoryClass.getDeclaredConstructor().newInstance();

        } catch (final IOException | ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to load repository factory", e);
        }
    }
}
