package pt.isep.sem4pi.application.service.weatherdata.importer;

public interface WeatherDataImporter {
    boolean validate(String filePath);
    WeatherImportPayload execute(String filePath);
}

