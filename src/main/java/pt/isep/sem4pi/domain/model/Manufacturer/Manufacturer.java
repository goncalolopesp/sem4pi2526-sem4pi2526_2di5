package pt.isep.sem4pi.domain.model.Manufacturer;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;

@Entity
public class Manufacturer implements AggregateRoot<ManufacturerId> {

    @EmbeddedId
    private ManufacturerId id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    @Version
    private Long version;

    protected Manufacturer() {
        // for ORM
    }

    public Manufacturer(ManufacturerId id, String name, String country) {
        if (id == null) {
            throw new IllegalArgumentException("The Manufacturer ID cannot be null!");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The Manufacturer name cannot be null or empty!");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("The Manufacturer country cannot be null or empty!");
        }
        this.id = id;
        this.name = name;
        this.country = country;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof Manufacturer)) return false;
        final Manufacturer that = (Manufacturer) other;

        return this.identity().equals(that.identity());
    }

    @Override
    public ManufacturerId identity() {
        return id;
    }
}