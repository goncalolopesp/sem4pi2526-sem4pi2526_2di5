package pt.isep.sem4pi.domain.model.aircraft;

import jakarta.persistence.*;

@Entity
@Table(name = "Aircraft")
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aircraftId;

    @Embedded
    @Column(unique = true, nullable = false)
    private AircraftRegistration registration;

    private String aircraftModelId;
    private String countryOfRegistration;

    @Embedded
    private CabinConfiguration cabinConfiguration;

    protected Aircraft() {}

    public Aircraft(AircraftRegistration registration, String aircraftModelId,
                    String countryOfRegistration, CabinConfiguration cabinConfiguration) {
        this.registration = registration;
        this.aircraftModelId = aircraftModelId;
        this.countryOfRegistration = countryOfRegistration;
        this.cabinConfiguration = cabinConfiguration;
    }

    public AircraftRegistration getRegistration() { return registration; }
}