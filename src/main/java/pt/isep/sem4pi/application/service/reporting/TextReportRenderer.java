package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.reporting.Report;
import pt.isep.sem4pi.domain.model.reporting.ReportSection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Escreve o relatório num ficheiro de texto.
 * Não procura dados nem decide as secções.
 */
public class TextReportRenderer implements ReportRenderer {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format() {
        return "TEXT";
    }

    @Override
    public String fileExtension() {
        return "txt";
    }

    /**
     * Escreve as secções num ficheiro de texto.
     */
    @Override
    public Path render(final Report report, final Path destination) throws IOException {
        final StringBuilder output = new StringBuilder();
        output.append("============================================================\n")
                .append("                     AICONTROL REPORT\n")
                .append("============================================================\n")
                .append("Title: ").append(report.title()).append('\n')
                .append("Period: ").append(report.period()).append('\n')
                .append("Generated at: ")
                .append(report.generatedAt().format(TIMESTAMP)).append('\n');

        for (final ReportSection section : report.sections()) {
            output.append("\n------------------------------------------------------------\n")
                    .append(section.title()).append('\n')
                    .append("------------------------------------------------------------\n");
            for (final ReportSection.Entry entry : section.entries()) {
                output.append(String.format("%-38s %s%n",
                        entry.label() + ":", entry.value()));
            }
        }

        output.append("\n============================================================\n");
        final Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        // A criação física do ficheiro fica no renderer, não no builder.
        Files.writeString(destination, output.toString(), StandardCharsets.UTF_8);
        return destination.toAbsolutePath().normalize();
    }
}
