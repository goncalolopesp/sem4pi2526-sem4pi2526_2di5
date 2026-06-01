package pt.isep.sem4pi.domain.weatherdata;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "WeatherImportBatch")
public class WeatherImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long batchId;

    @Column(name = "air_control_area_id", nullable = false)
    private String airControlAreaId;

    @Column(nullable = false)
    private LocalDateTime importedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportBatchStatus status;

    @Column(nullable = false)
    private int importedRecords;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "batch_id")
    private List<WeatherData> records = new ArrayList<>();

    protected WeatherImportBatch() {}

    public WeatherImportBatch(String airControlAreaId, WeatherData manualEntry) {
        if (airControlAreaId == null || manualEntry == null) {
            throw new IllegalArgumentException("Area ID and Weather Data are required.");
        }
        this.airControlAreaId = airControlAreaId;
        this.importedAt = LocalDateTime.now();
        this.status = ImportBatchStatus.COMPLETED;
        this.records.add(manualEntry);
        this.importedRecords = 1;
    }

    public WeatherImportBatch(String airControlAreaId, List<WeatherData> records, ImportBatchStatus status) {
        if (airControlAreaId == null || airControlAreaId.trim().isEmpty()) {
            throw new IllegalArgumentException("Area ID is required.");
        }
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("At least one weather record is required.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Import status is required.");
        }
        this.airControlAreaId = airControlAreaId;
        this.importedAt = LocalDateTime.now();
        this.status = status;
        this.records.addAll(records);
        this.importedRecords = records.size();
    }

    public static WeatherImportBatch forBulkImport(String airControlAreaId, List<WeatherData> records) {
        return new WeatherImportBatch(airControlAreaId, records, ImportBatchStatus.COMPLETED);
    }

    public String getAirControlAreaId() { return airControlAreaId; }
    public int getImportedRecords() { return importedRecords; }
    public ImportBatchStatus getStatus() { return status; }
    public List<WeatherData> getRecords() { return records; }
}