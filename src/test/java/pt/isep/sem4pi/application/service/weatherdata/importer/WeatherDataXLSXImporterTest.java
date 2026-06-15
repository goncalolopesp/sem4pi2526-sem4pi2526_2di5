package pt.isep.sem4pi.application.service.weatherdata.importer;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Parser tests for the US042 XLSX weather importer.
 *
 * <p>The XLSX importer has to detect the wind-data header inside a workbook
 * and handle both textual and Excel-native date/time values.</p>
 */
public class WeatherDataXLSXImporterTest {

    /**
     * Confirms that the provided sample workbook is a supported weather source.
     */
    @Test
    public void ensureImporterParsesKnownXlsxFile() {
        String filePath = Path.of("src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData",
                "HP_WeatherData_v0.xlsx").toString();

        WeatherDataXLSXImporter importer = new WeatherDataXLSXImporter();
        assertTrue(importer.validate(filePath));

        WeatherImportPayload payload = importer.execute(filePath);
        assertFalse(payload.getRecords().isEmpty());
        assertTrue(payload.getTotalRecords() > 0);
    }

    /**
     * Builds a minimal workbook to prove that the importer preserves the start
     * and end interval used by consultation and association screens.
     */
    @Test
    public void ensureImporterParsesStartAndEndTimes() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("weather", ".xlsx");
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Wind");
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            String[] cols = new String[]{"ACA", "Lat.", "Long.", "Lat.", "Long.", "Alt_inf (ft)", "Alt_Sup (ft)",
                    "Direction (degree)", "Value (knot)", "Day", "Star", "End"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("121");
            row.createCell(5).setCellValue(0);
            row.createCell(6).setCellValue(1000);
            row.createCell(7).setCellValue(90);
            row.createCell(8).setCellValue(28.75);
            row.createCell(9).setCellValue("6/22/2026");
            row.createCell(10).setCellValue("5:00");
            row.createCell(11).setCellValue("8:15");

            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(tempFile)) {
                workbook.write(out);
            }
        }

        WeatherDataXLSXImporter importer = new WeatherDataXLSXImporter();
        WeatherImportPayload payload = importer.execute(tempFile.toString());

        assertFalse(payload.getRecords().isEmpty());
        assertEquals(java.time.LocalTime.of(5, 0), payload.getRecords().get(0).getStartTime());
        assertEquals(java.time.LocalTime.of(8, 15), payload.getRecords().get(0).getEndTime());
    }

    /**
     * Verifies the same invalid-time rule as CSV: without a valid interval,
     * the row is reported as an error.
     */
    @Test
    public void ensureImporterReportsInvalidTimeRange() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("weather", ".xlsx");
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Wind");
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            String[] cols = new String[]{"ACA", "Lat.", "Long.", "Lat.", "Long.", "Alt_inf (ft)", "Alt_Sup (ft)",
                    "Direction (degree)", "Value (knot)", "Day", "Star", "End"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("121");
            row.createCell(5).setCellValue(0);
            row.createCell(6).setCellValue(1000);
            row.createCell(7).setCellValue(90);
            row.createCell(8).setCellValue(28.75);
            row.createCell(9).setCellValue("6/22/2026");
            row.createCell(10).setCellValue("");
            row.createCell(11).setCellValue("");

            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(tempFile)) {
                workbook.write(out);
            }
        }

        WeatherDataXLSXImporter importer = new WeatherDataXLSXImporter();
        WeatherImportPayload payload = importer.execute(tempFile.toString());

        assertEquals(0, payload.getRecords().size());
        assertEquals(1, payload.getErrors().size());
    }
}
