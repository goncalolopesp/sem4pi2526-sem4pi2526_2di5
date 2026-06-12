# US112 - Monthly Report Generation

## 1. Purpose

US112 generates a monthly statistics report from data already stored by
AIControl. The report is exported as a text file and its metadata is persisted
for later consultation.

The console application has no graphical reporting library. Therefore, the
current renderer uses branded text sections and aligned summaries. The
rendering contract allows a future graphical, HTML, PDF, or chart renderer to
be added without changing monthly data collection.

## 2. Architecture

The implementation follows the EAPLI layers:

1. **Presentation** - `GenerateMonthlyReportUI` reads the month and displays a
   `GeneratedReportDTO`.
2. **Controller** - `GenerateMonthlyReportController` checks authorization and
   invokes the application service.
3. **Application** - `GenerateMonthlyReportService` coordinates collection,
   report construction, rendering, file storage, and metadata persistence.
4. **Domain** - `Report`, `ReportSection`, and `ReportMetadata` define the
   report structure and persisted metadata aggregate.
5. **Repositories** - `ReportMetadataRepository` has JPA and in-memory
   implementations obtained through `RepositoryFactory`.

Metadata is saved inside an explicit `TransactionalContext`. If metadata
persistence fails, the generated file is deleted so that the file system and
database do not present a successful report inconsistently.

## 3. Separation of Responsibilities

### Data Collection

`ReportDataCollector<T>` is independent from presentation and export.
`MonthlyStatisticsDataCollector` reads:

- simulations whose `startTime` belongs to the selected month;
- simulation reports whose `generatedAt` belongs to the selected month;
- safety violation totals already present in those US111-compatible report
  files;
- flights whose `departureDate` belongs to the selected month.

No values are estimated or invented. If an old or unavailable simulation
report file has no readable violation total, it contributes zero violations.

### Report Structure

`MonthlyStatisticsReportBuilder` converts `MonthlyStatisticsData` into a
`MonthlyStatisticsReport` containing clearly separated `ReportSection`
objects. It does not access repositories or write files.

### Rendering

`ReportRenderer` exports any `Report`. `TextReportRenderer` provides the
current console-compatible format and consistent AIControl heading,
title, period, generation timestamp, and section layout.

## 4. Extending the Framework

To add another report type, such as compliance or incident reporting:

1. Create its data object and a `ReportDataCollector` implementation.
2. Create a `Report` subtype or builder that defines its sections.
3. Reuse an existing renderer.
4. Add only its controller/application composition.

To add a new format, implement `ReportRenderer` and inject it into the
application service. Monthly collection and section-building code remain
unchanged.

## 5. Output and Compatibility

Generated files are placed in `reports/` and use a timestamped name. The
simulation report parser and US111 report files are not modified. US112 only
reads the existing `Total flights`, generation metadata, validation result,
and US102 safety violation total already made available through US111.

## 6. Tests

The automated tests cover:

- report generation with data;
- report generation without data;
- text file creation and content;
- transactional metadata persistence;
- safety violation extraction from existing simulation report files;
- extensibility through a second renderer implementation.
