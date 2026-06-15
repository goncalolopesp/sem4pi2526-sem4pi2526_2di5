// Existe na US112 como contrato de renderização, separando o formato da construção do relatório.
package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.reporting.Report;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Define como o relatório é escrito num ficheiro.
 * Permite usar texto, PDF ou HTML sem mudar o serviço.
 */
public interface ReportRenderer {

    /**
     * Indica o formato do relatório.
     */
    String format();

    /**
     * Indica a extensão usada no nome do ficheiro.
     */
    String fileExtension();

    /**
     * Escreve o relatório no local indicado.
     */
    Path render(Report report, Path destination) throws IOException;
}
