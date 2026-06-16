package pt.isep.sem4pi.application.service.reporting;

import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import pt.isep.sem4pi.application.viadto.GeneratedReportDTO;
import pt.isep.sem4pi.domain.model.reporting.Report;
import pt.isep.sem4pi.domain.model.reporting.ReportMetadata;
import pt.isep.sem4pi.domain.repositories.ReportMetadataRepository;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do serviço principal da US112.
 * Confirmam criação do ficheiro, transação e possibilidade de trocar renderer.
 */
public class GenerateMonthlyReportServiceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Garante que o relatório é escrito e os metadados ficam gravados na BD.
     */
    @Test
    public void createsFileAndPersistsMetadataTransactionally() throws Exception {
        final TransactionalContext tx = mock(TransactionalContext.class);
        final ReportMetadataRepository metadataRepository =
                mock(ReportMetadataRepository.class);
        final RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.newTransactionalContext()).thenReturn(tx);
        when(repositoryFactory.reportMetadata(tx)).thenReturn(metadataRepository);

        final MonthlyStatisticsData data = new MonthlyStatisticsData(
                0, Map.of(), 0, 0, 0, 0, 0, Map.of());
        final ReportDataCollector<MonthlyStatisticsData> collector = period -> data;
        final Path outputDirectory = temporaryFolder.newFolder("reports").toPath();
        final Clock clock = Clock.fixed(
                Instant.parse("2026-06-12T10:15:30Z"), ZoneOffset.UTC);

        final GenerateMonthlyReportService service = new GenerateMonthlyReportService(
                collector,
                new MonthlyStatisticsReportBuilder(),
                new TextReportRenderer(),
                repositoryFactory,
                outputDirectory,
                clock);

        final GeneratedReportDTO result = service.generate(YearMonth.of(2026, 6));

        assertTrue(Files.exists(Path.of(result.fileLocation())));
        final String content = Files.readString(Path.of(result.fileLocation()));
        assertTrue(content.contains("AIControl Monthly Statistics Report"));
        assertTrue(content.contains("Period: 2026-06"));
        assertTrue(content.contains("Simulations started:"));
        verify(tx).beginTransaction();
        verify(tx).commit();
        verify(tx).close();

        final ArgumentCaptor<ReportMetadata> metadata =
                ArgumentCaptor.forClass(ReportMetadata.class);
        verify(metadataRepository).save(metadata.capture());
        assertEquals(result.reportId(), metadata.getValue().reportId());
        assertEquals(result.fileLocation(), metadata.getValue().fileLocation());
    }

    /**
     * Mostra que o formato pode mudar sem alterar a lógica do relatório mensal.
     */
    @Test
    public void acceptsANewRendererWithoutChangingMonthlyReportLogic() throws Exception {
        final TransactionalContext tx = mock(TransactionalContext.class);
        final ReportMetadataRepository metadataRepository =
                mock(ReportMetadataRepository.class);
        final RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.newTransactionalContext()).thenReturn(tx);
        when(repositoryFactory.reportMetadata(tx)).thenReturn(metadataRepository);

        final ReportRenderer alternateRenderer = new ReportRenderer() {
            @Override
            public String format() {
                return "TEST";
            }

            @Override
            public String fileExtension() {
                return "test";
            }

            @Override
            public Path render(final Report report, final Path destination)
                    throws java.io.IOException {
                Files.createDirectories(destination.getParent());
                Files.writeString(destination, report.type());
                return destination.toAbsolutePath();
            }
        };

        final GenerateMonthlyReportService service = new GenerateMonthlyReportService(
                period -> new MonthlyStatisticsData(
                        0, Map.of(), 0, 0, 0, 0, 0, Map.of()),
                new MonthlyStatisticsReportBuilder(),
                alternateRenderer,
                repositoryFactory,
                temporaryFolder.newFolder("alternate").toPath(),
                Clock.fixed(Instant.parse("2026-06-12T10:15:30Z"), ZoneOffset.UTC));

        final GeneratedReportDTO result = service.generate(YearMonth.of(2026, 6));

        assertTrue(result.fileLocation().endsWith(".test"));
        assertEquals("TEST", result.format());
        assertEquals("MONTHLY_STATISTICS",
                Files.readString(Path.of(result.fileLocation())));
    }
}
