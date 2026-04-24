package pt.isep.sem4pi.infrastructure.persistence;

/**
 * Simple persistence context that exposes a RepositoryFactory.
 * For US030 we default to the in-memory factory.
 */
public final class PersistenceContext {

    private static RepositoryFactory theFactory;

    private PersistenceContext() {
        // utility
    }

    public static RepositoryFactory repositories() {
        if (theFactory == null) {
            theFactory = new InMemoryRepositoryFactory();
        }
        return theFactory;
    }
}

