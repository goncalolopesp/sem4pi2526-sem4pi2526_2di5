package pt.isep.sem4pi.application.service.weatherdata.importer;

import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado intermmedio produzido por um parser de ficheiros da US042.
 *
 * <p>Mantem propositadamente registos bem-sucedidos e erros em conjunto,
 * permitindo uma importacao parcial. Cada registo ou erro adicionado representa
 * uma linha de origem processada e por isso incrementa {@code totalRecords}.</p>
 */
public class WeatherImportPayload {
    private final List<WeatherDataDTO> records = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private int totalRecords;

    /**
     * Regista uma linha de origem analisada com sucesso.
     *
     * @param dto objeto de transporte que mantem a linha de origem original
     */
    public void addRecord(WeatherDataDTO dto) {
        records.add(dto);
        totalRecords++;
    }

    /**
     * Regista uma linha de origem que nao conseguiu ser analisada.
     *
     * @param message mensagem de diagnostico, normalmente com o numero da linha
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

