package pt.isep.sem4pi.domain.aircraft;

import jakarta.persistence.Embeddable;

@Embeddable
public class CabinConfiguration {
    private int economySeats;
    private int businessSeats;
    private int firstClassSeats;

    protected CabinConfiguration() {}

    public CabinConfiguration(int eco, int bus, int first) {
        if (eco < 0 || bus < 0 || first < 0) throw new IllegalArgumentException("Seats cannot be negative.");
        if (eco + bus + first == 0) throw new IllegalArgumentException("Aircraft must have at least 1 seat.");
        this.economySeats = eco;
        this.businessSeats = bus;
        this.firstClassSeats = first;
    }

    public int getTotalCapacity() {
        return economySeats + businessSeats + firstClassSeats;
    }
}