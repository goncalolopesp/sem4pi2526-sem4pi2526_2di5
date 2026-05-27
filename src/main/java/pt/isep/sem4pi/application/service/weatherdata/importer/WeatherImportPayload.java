package pt.isep.sem4pi.application.service.weatherdata.importer;

import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeatherImportPayload {
    private final List<WeatherDataDTO> records = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private int totalRecords;

    public void addRecord(WeatherDataDTO dto) {
        records.add(dto);
        totalRecords++;
    }

    public void addError(String message) {
        errors.add(message);
        totalRecords++;
    }

    public List<WeatherDataDTO> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public int getTotalRecords() {
        return totalRecords;
    }
}

