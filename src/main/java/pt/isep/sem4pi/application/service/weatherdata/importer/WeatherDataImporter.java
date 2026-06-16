package pt.isep.sem4pi.application.service.weatherdata.importer;

/**
 * Estrategia usada pela US042 para isolar cada formato de ficheiro meteorologico.
 *
 * <p>As implementacoes apenas fazem o parsing dos dados de origem. A validacao
 * de dominio, criacao de areas e persistencia continuam a ser responsabilidades
 * do servico aplicacional.</p>
 */
public interface WeatherDataImporter {
    /**
     * Verifica se a fonte existe, e legivel e tem o tipo esperado.
     *
     * @param filePath caminho do ficheiro de origem
     * @return {@code true} quando este importador consegue ler a fonte
     */
    boolean validate(String filePath);

    /**
     * Faz o parsing da fonte completa, preservando linhas validas e erros.
     *
     * @param filePath caminho do ficheiro de origem
     * @return registos analisados e erros encontrados
     */
    WeatherImportPayload execute(String filePath);
}

