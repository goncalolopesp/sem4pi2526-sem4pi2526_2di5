package pt.isep.sem4pi.domain.manufacturer;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
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
        Preconditions.nonNull(id, "The Manufacturer ID cannot be null!");
        Preconditions.ensure(name != null && !name.trim().isEmpty(),
                "The Manufacturer name cannot be null or empty!");
        Preconditions.ensure(country != null && !country.trim().isEmpty(),
                "The Manufacturer country cannot be null or empty!");
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

    public String name() {
        return name;
    }

    public String country() {
        return country;
    }

    @Override
    public String toString() {
        return name + " (" + country + ")";
    }
}