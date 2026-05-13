package pt.isep.sem4pi.infrastructure.persistence;

import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PersistenceContext {

    private static RepositoryFactory theFactory;

    private PersistenceContext() {
        // utility
    }

    public static RepositoryFactory repositories() {
        if (theFactory == null) {
            theFactory = buildRepositoryFactory();
        }

        return theFactory;
    }

    private static RepositoryFactory buildRepositoryFactory() {
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