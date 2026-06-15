package pt.isep.sem4pi.application.service.weatherdata.importer;

import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Intermediate result produced by a US042 file parser.
 *
 * <p>It deliberately keeps successful records and errors together, allowing a
 * partial import. Every added record or error represents one processed source
 * row and therefore increments {@code totalRecords}.</p>
 */
public class WeatherImportPayload {
    private final List<WeatherDataDTO> records = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private int totalRecords;

    /**
     * Registers one successfully parsed source row.
     *
     * @param dto transport object retaining the original source line
     */
    public void addRecord(WeatherDataDTO dto) {
        records.add(dto);
        totalRecords++;
    }

    /**
     * Registers one source row that could not be parsed.
     *
     * @param message diagnostic message, normally including the line number
     */
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

