package pt.isep.sem4pi.application.service.weatherdata;

import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherDataImporter;
import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherDataImporterFactory;
import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherImportPayload;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaBoundary;
import pt.isep.sem4pi.domain.model.aircontrolarea.DTO.AirControlAreaCreateDTO;
import pt.isep.sem4pi.domain.model.weatherdata.*;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataDTO;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.util.*;

/**
 * Coordena a importacao de dados meteorologicos da US042.
 *
 * <p>O servico separa o parsing do ficheiro da criacao de objetos de dominio.
 * Um importador converte cada linha da fonte num DTO; depois este servico
 * verifica a Air Control Area referenciada, cria areas em falta quando existem
 * dados ACA auxiliares, constroi objetos de dominio validados e persiste um
 * {@link WeatherImportBatch} por area.</p>
 *
 * <p>Linhas invalidas nao cancelam a importacao completa. Os seus erros sao
 * recolhidos para que as linhas validas ainda possam ser persistidas e
 * apresentadas ao utilizador.</p>
 */
public class WeatherDataImportService {

    private final WeatherBatchRepository batchRepository;
    private final AirControlAreaRepository airControlAreaRepository;
    private final WeatherDataImporterFactory importerFactory;

    public WeatherDataImportService(WeatherBatchRepository batchRepository,
                                    AirControlAreaRepository airControlAreaRepository,
                                    WeatherDataImporterFactory importerFactory) {
        this.batchRepository = batchRepository;
        this.airControlAreaRepository = airControlAreaRepository;
        this.importerFactory = importerFactory;
    }

    /**
     * Importa todos os registos validos encontrados na fonte fornecida.
     *
     * <p>Os registos sao agrupados por Air Control Area porque
     * {@code WeatherImportBatch} e o aggregate root que possui os dados
     * meteorologicos de uma area. A contagem de rejeitados inclui falhas de
     * parsing, de dominio e de areas em falta.</p>
     *
     * @param filePath caminho da fonte ou nome do ficheiro
     * @param fileType tipo CSV/XLSX, opcionalmente inferido pelo caminho
     * @return resumo imutavel da importacao para a camada de apresentacao
     * @throws IllegalArgumentException quando a fonte nao pode ser validada ou
     *                                  o formato nao e suportado
     */
    public WeatherImportResultDTO importWeatherData(String filePath, String fileType) {
        String resolvedPath = resolveFilePath(filePath, fileType);
        java.util.Map<String, AirControlAreaBoundary> acaDefinitions =
                loadAirControlAreas(resolvedPath, fileType);
        java.util.Set<String> createdAreas = new java.util.HashSet<>();
        java.util.Set<String> missingAreaErrors = new java.util.HashSet<>();

        WeatherDataImporter importer = importerFactory.createImporter(fileType, resolvedPath);
        if (!importer.validate(resolvedPath)) {
            throw new IllegalArgumentException("Invalid file path or type: " + filePath);
        }

        WeatherImportPayload payload = importer.execute(resolvedPath);
        Map<String, List<WeatherData>> recordsByArea = new HashMap<>();
        List<String> errors = new ArrayList<>(payload.getErrors());
        int imported = 0;

        for (WeatherDataDTO dto : payload.getRecords()) {
            String areaId = dto.getAirControlAreaId();
            if (!airControlAreaRepository.containsOfIdentity(new AirControlAreaID(areaId))) {
                if (!createdAreas.contains(areaId) && acaDefinitions.containsKey(areaId)) {
                    try {
                        AirControlAreaBoundary boundary = acaDefinitions.get(areaId);
                        AirControlAreaCreateDTO createDto = new AirControlAreaCreateDTO(
                                areaId,
                                IMPORTED_ACA_NAME_PREFIX + areaId,
                                boundary.getMinLatitude(),
                                boundary.getMaxLatitude(),
                                boundary.getMinLongitude(),
                                boundary.getMaxLongitude(),
                                IMPORTED_ACA_MIN_RESERVE_FUEL
                        );
                        AirControlArea area = createAirControlArea(createDto);
                        airControlAreaRepository.save(area);
                        createdAreas.add(areaId);
                    } catch (IllegalArgumentException e) {
                        errors.add("Line " + dto.getSourceLine() + ": Failed to create Air Control Area '" + areaId + "' - " + e.getMessage());
                        continue;
                    }
                }
            }

            if (!airControlAreaRepository.containsOfIdentity(new AirControlAreaID(areaId))) {
                if (missingAreaErrors.add(areaId)) {
                    errors.add("Line " + dto.getSourceLine() + ": Air Control Area '" + areaId + "' does not exist.");
                }
                continue;
            }

            try {
                WeatherData data = new WeatherData(
                        dto.getDate(),
                        dto.getStartTime(),
                        dto.getEndTime(),
                        new Wind(dto.getWindSpeedKnots(), dto.getWindDirectionDegrees()),
                        new Temperature(dto.getTemperatureCelsius()),
                        new AtmosphericPressure(dto.getPressureHpa()),
                        new Humidity(dto.getHumidityPercentage()),
                        new Altitude(dto.getAltitudeMeters())
                );
                recordsByArea.computeIfAbsent(areaId, key -> new ArrayList<>()).add(data);
                imported++;
            } catch (IllegalArgumentException e) {
                errors.add("Line " + dto.getSourceLine() + ": " + e.getMessage());
            }
        }

        for (Map.Entry<String, List<WeatherData>> entry : recordsByArea.entrySet()) {
            List<WeatherData> records = entry.getValue();
            if (records.isEmpty()) {
                continue;
            }
            WeatherImportBatch batch = WeatherImportBatch.forBulkImport(entry.getKey(), records);
            batchRepository.save(batch);
        }

        int total = payload.getTotalRecords();
        int rejected = Math.max(0, total - imported);

        return new WeatherImportResultDTO(total, imported, rejected, errors, new ArrayList<>(recordsByArea.keySet()));
    }

    private String resolveFilePath(String filePath, String fileType) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return filePath;
        }
        String trimmed = filePath.trim();
        String normalizedType = fileType == null ? "" : fileType.trim().toLowerCase();

        if (!trimmed.contains(".")) {
            if (!normalizedType.isEmpty()) {
                trimmed = trimmed + "." + normalizedType;
            } else {
                String resolved = tryResolveWithExtensions(trimmed, new String[]{"xlsx", "csv"});
                if (resolved != null) {
                    return resolved;
                }
            }
        }

        java.nio.file.Path path = java.nio.file.Path.of(trimmed);
        if (path.isAbsolute() || java.nio.file.Files.exists(path)) {
            return trimmed;
        }

        java.nio.file.Path dataPath = java.nio.file.Path.of(
                "src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData", trimmed
        );
        if (java.nio.file.Files.exists(dataPath)) {
            return dataPath.toString();
        }

        return trimmed;
    }

    private String tryResolveWithExtensions(String baseName, String[] extensions) {
        for (String extension : extensions) {
            String candidate = baseName + "." + extension;
            java.nio.file.Path direct = java.nio.file.Path.of(candidate);
            if (java.nio.file.Files.exists(direct)) {
                return candidate;
            }
            java.nio.file.Path dataPath = java.nio.file.Path.of(
                    "src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData", candidate
            );
            if (java.nio.file.Files.exists(dataPath)) {
                return dataPath.toString();
            }
        }
        return null;
    }

    private static final String IMPORTED_ACA_NAME_PREFIX = "ACA ";
    private static final double IMPORTED_ACA_MIN_RESERVE_FUEL = 0.0;

    private java.util.Map<String, AirControlAreaBoundary> loadAirControlAreas(
            String weatherFilePath, String fileType) {
        if (weatherFilePath == null || weatherFilePath.trim().isEmpty()) {
            return java.util.Map.of();
        }
        String extension = extractExtension(weatherFilePath, fileType);
        if ("csv".equals(extension)) {
            String acaPath = resolveAcaFilePath(weatherFilePath, "csv");
            if (acaPath == null) {
                return java.util.Map.of();
            }
            return new pt.isep.sem4pi.application.service.weatherdata.importer.AirControlAreaCSVImporter()
                    .loadAreas(acaPath);
        }
        if ("xlsx".equals(extension)) {
            return new pt.isep.sem4pi.application.service.weatherdata.importer.AirControlAreaXLSXImporter()
                    .loadAreas(weatherFilePath);
        }
        return java.util.Map.of();
    }

    private String resolveAcaFilePath(String weatherFilePath, String extension) {
        java.nio.file.Path path = java.nio.file.Path.of(weatherFilePath);
        String fileName = path.getFileName().toString();
        String baseName = fileName;
        if (fileName.toLowerCase().endsWith("." + extension)) {
            baseName = fileName.substring(0, fileName.length() - (extension.length() + 1));
        }
        String candidateName = baseName;
        if (baseName.toLowerCase().contains("-wind")) {
            candidateName = baseName.replaceFirst("(?i)-wind", "-ACA");
        } else if (!baseName.toLowerCase().contains("-aca")) {
            candidateName = baseName + "-ACA";
        }

        String candidateFile = candidateName + "." + extension;
        java.nio.file.Path candidatePath = path.resolveSibling(candidateFile);
        if (java.nio.file.Files.exists(candidatePath)) {
            return candidatePath.toString();
        }

        java.nio.file.Path dataPath = java.nio.file.Path.of(
                "src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData", candidateFile
        );
        if (java.nio.file.Files.exists(dataPath)) {
            return dataPath.toString();
        }

        return null;
    }

    private String extractExtension(String filePath, String fileType) {
        if (fileType != null && !fileType.trim().isEmpty()) {
            return fileType.trim().toLowerCase();
        }
        int idx = filePath.lastIndexOf('.');
        if (idx < 0 || idx == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(idx + 1).toLowerCase();
    }

    private AirControlArea createAirControlArea(AirControlAreaCreateDTO dto) {
        AirControlAreaBoundary boundaries =
                new AirControlAreaBoundary(
                        dto.getMinLatitude(), dto.getMaxLatitude(), dto.getMinLongitude(), dto.getMaxLongitude());
        return new AirControlArea(
                dto.getCode(), boundaries, dto.getName(), dto.getMinimumReserveFuel());
    }
}
