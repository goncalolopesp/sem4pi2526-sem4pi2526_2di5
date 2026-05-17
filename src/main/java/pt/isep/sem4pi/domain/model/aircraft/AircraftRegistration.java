package pt.isep.sem4pi.domain.model.aircraft;

import jakarta.persistence.Embeddable;

@Embeddable
public class AircraftRegistration {
    private String registration;

    protected AircraftRegistration() {}

    public AircraftRegistration(String registration) {
        if (registration == null || registration.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration cannot be empty.");
        }
        this.registration = registration.toUpperCase();
    }

    public String getRegistration() { return registration; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftRegistration)) return false;
        return registration.equals(((AircraftRegistration) o).registration);
    }
}