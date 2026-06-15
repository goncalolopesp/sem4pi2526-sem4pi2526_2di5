package pt.isep.sem4pi.presentation.reporting;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.reporting.GenerateMonthlyReportController;
import pt.isep.sem4pi.application.viadto.GeneratedReportDTO;

public class GenerateMonthlyReportUI extends AbstractUI {

    private final GenerateMonthlyReportController controller =
            new GenerateMonthlyReportController();

    @Override
    protected boolean doShow() {
        final int year = Console.readInteger("Year:");
        final int month = Console.readInteger("Month (1-12):");
        try {
            final GeneratedReportDTO report = controller.generate(year, month);
            System.out.println("Report generated successfully.");
            System.out.println("Report id: " + report.reportId());
            System.out.println("File: " + report.fileLocation());
            return true;
        } catch (final Exception exception) {
            System.out.println("Unable to generate report: " + exception.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Generate Monthly Statistics Report";
    }
}
