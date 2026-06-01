package pt.isep.sem4pi.presentation.weatherdata;

import pt.isep.sem4pi.application.controller.weatherdata.ImportWeatherDataController;
import pt.isep.sem4pi.domain.weatherdata.DTO.WeatherImportResultDTO;

public class ImportWeatherDataRunner {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ImportWeatherDataRunner <filePath> [fileType]");
            return;
        }
        String filePath = args[0];
        String fileType = args.length > 1 ? args[1] : "";

        ImportWeatherDataController controller = new ImportWeatherDataController();
        try {
            WeatherImportResultDTO result = controller.importWeatherData(filePath, fileType);
            System.out.println("Import completed.");
            System.out.println("Total records: " + result.getTotalRecords());
            System.out.println("Imported: " + result.getImportedRecords());
            System.out.println("Rejected: " + result.getRejectedRecords());
            if (!result.getErrors().isEmpty()) {
                System.out.println("Errors:");
                for (String error : result.getErrors()) {
                    System.out.println(" - " + error);
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Import failed: " + e.getMessage());
        }
    }
}

