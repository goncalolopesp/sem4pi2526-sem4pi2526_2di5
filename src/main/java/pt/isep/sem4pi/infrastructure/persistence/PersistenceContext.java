package pt.isep.sem4pi.infrastructure.persistence;

/**
 * Contexto de persistencia do projeto.
 *
 * Exponha a fabrica de repositorios usada pela aplicacao. Por omissao usa a implementacao em memoria.
 */
public final class PersistenceContext {

    private static RepositoryFactory theFactory;

    private PersistenceContext() {
        // utility
    }

    /**
     * Retorna a fabrica de repositorios actual.
     * Se ainda nao existir, inicializa a fabrica em memoria por omissao.
     * @return RepositoryFactory usado pela aplicacao
     */
    public static RepositoryFactory repositories() {
        if (theFactory == null) {
            theFactory = new InMemoryRepositoryFactory();
        }
        return theFactory;
    }
}
