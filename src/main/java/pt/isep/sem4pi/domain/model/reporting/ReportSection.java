package pt.isep.sem4pi.domain.model.reporting;

import eapli.framework.validations.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReportSection {

    // DEFESA:
    // Este Value Object representa uma secção por valor e expõe coleções não modificáveis.
    private final String title;
    private final List<Entry> entries;

    public ReportSection(final String title, final List<Entry> entries) {
        Preconditions.nonEmpty(title, "Section title is required");
        Preconditions.nonNull(entries, "Section entries are required");
        this.title = title.trim();
        this.entries = new ArrayList<>(entries);
    }

    public static ReportSection of(final String title, final Entry... entries) {
        return new ReportSection(title, List.of(entries));
    }

    public String title() {
        return title;
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public record Entry(String label, String value) {
        public Entry {
            Preconditions.nonEmpty(label, "Entry label is required");
            Preconditions.nonNull(value, "Entry value is required");
            label = label.trim();
        }

        public static Entry of(final String label, final Object value) {
            return new Entry(label, String.valueOf(value));
        }
    }
}
