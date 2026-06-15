package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.reporting.Report;

import java.io.IOException;
import java.nio.file.Path;

public interface ReportRenderer {

    // DEFESA:
    // Strategy e Protected Variations: TEXT pode ser substituído por HTML/PDF sem mudar recolha ou domínio.
    String format();

    String fileExtension();

    Path render(Report report, Path destination) throws IOException;
}
