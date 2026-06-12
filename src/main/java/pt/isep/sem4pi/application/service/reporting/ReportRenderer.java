package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.reporting.Report;

import java.io.IOException;
import java.nio.file.Path;

public interface ReportRenderer {

    String format();

    String fileExtension();

    Path render(Report report, Path destination) throws IOException;
}
