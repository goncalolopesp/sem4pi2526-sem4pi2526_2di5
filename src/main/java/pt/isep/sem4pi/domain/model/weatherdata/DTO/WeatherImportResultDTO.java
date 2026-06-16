package pt.isep.sem4pi.domain.model.weatherdata.DTO;

import java.util.Collections;
import java.util.List;

/**
 * Resultado apenas de leitura devolvido pela US042 apos uma importacao meteorologica em massa.
 *
 * <p>Da a UI informacao suficiente para explicar um sucesso parcial sem expor
 * objetos de importacao ou de persistencia.</p>
 */
public class WeatherImportResultDTO {
    private final int totalRecords;
    private final int importedRecords;
    private final int rejectedRecords;
    private final List<String> errors;
    private final List<String> importedAreaIds;

    public WeatherImportResultDTO(int totalRecords, int importedRecords, int rejectedRecords,
                                  List<String> errors, List<String> importedAreaIds) {
        this.totalRecords = totalRecords;
        this.importedRecords = importedRecords;
        this.rejectedRecords = rejectedRecords;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
        this.importedAreaIds = importedAreaIds == null ? List.of() : List.copyOf(importedAreaIds);
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getImportedRecords() {
        return importedRecords;
    }

    public int getRejectedRecords() {
        return rejectedRecords;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> getImportedAreaIds() {
        return Collections.unmodifiableList(importedAreaIds);
    }
}

