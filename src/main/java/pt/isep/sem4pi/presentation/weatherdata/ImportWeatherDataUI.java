package pt.isep.sem4pi.presentation.weatherdata;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.weatherdata.ImportWeatherDataController;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;

import java.util.List;
import java.util.Scanner;

public class ImportWeatherDataUI extends AbstractUI {
    private final ImportWeatherDataController controller = new ImportWeatherDataController();
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public boolean doShow() {
        System.out.println("=== Import Weather Data (Bulk) ===");
        System.out.print("Enter file path (full path or filename in data/weatherData): ");
        String filePath = scanner.nextLine();
        System.out.print("Enter file type (csv/xlsx, leave blank to auto-detect): ");
        String fileType = scanner.nextLine();

        try {
            WeatherImportResultDTO result = controller.importWeatherData(filePath, fileType);
            printResult(result);
            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("\n[ERROR] Import failed: " + e.getMessage());
            return false;
        }
    }

    private void printResult(WeatherImportResultDTO result) {
        System.out.println("\n[SUCCESS] Import completed.");
        System.out.println("Total records: " + result.getTotalRecords());
        System.out.println("Imported: " + result.getImportedRecords());
        System.out.println("Rejected: " + result.getRejectedRecords());

        List<String> errors = result.getErrors();
        if (!errors.isEmpty()) {
            System.out.println("\nErrors:");
            for (String error : errors) {
                System.out.println(" - " + error);
            }
        }
    }

    @Override
    public String headline() {
        return "Import Weather Data";
    }
}
