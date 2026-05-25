package pt.isep.sem4pi.application.controller.weatherdata;

import pt.isep.sem4pi.application.service.weatherdata.WeatherDataImportService;
import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherDataImporterFactory;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class ImportWeatherDataController {
    private final WeatherDataImportService service;

    public ImportWeatherDataController() {
        this.service = new WeatherDataImportService(
                PersistenceContext.repositories().weatherBatches(),
                PersistenceContext.repositories().airControlAreas(),
                new WeatherDataImporterFactory()
        );
    }

    public WeatherImportResultDTO importWeatherData(String filePath, String fileType) {
        return service.importWeatherData(filePath, fileType);
    }
}

