package pt.isep.sem4pi.domain.model.airtransportcompany;

import jakarta.persistence.Embeddable;

@Embeddable
public class CompanyName {
    private String name;

    protected CompanyName() {}

    public CompanyName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty.");
        }
        this.name = name;
    }

    public String name() {
        return name;
    }
}
