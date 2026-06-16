package pt.isep.sem4pi.domain.model.flight;


import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Date;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "FlightPlan")
public class FlightPlan implements AggregateRoot<FlightPlanId> {

    @EmbeddedId
    private FlightPlanId flightPlanId;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "extracted_flight_id"))
    private FlightId extractedflightID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightType flightType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_plan_id", nullable = false)
    private List<FlightLeg> legs = new ArrayList<>();

    @Embedded
    private Date createdAt;

    @ManyToOne(optional = false)
    private FlightRoute route;

    @ManyToOne(optional = false)
    private Aircraft aircraft;

    @ManyToOne(optional = false)
    private Pilot pilot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightPlanStatus status;

    @ManyToMany
    @JoinTable(
            name = "flight_plan_weather_data",
            joinColumns = @JoinColumn(name = "flight_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "weather_data_id")
    )
    private List<WeatherData> weatherData = new ArrayList<>();

    @Column(name = "dsl_source_path")
    private String dslSourcePath;

    @Version
    private Long version;

    protected FlightPlan(){
        //for ORM
    }

    public FlightPlan(FlightPlanId flightPlanId, FlightId flightID, FlightType type, FlightRoute route,
                      List<FlightLeg> flightLegs, Aircraft aircraft, Pilot pilot){
        Preconditions.nonNull(flightPlanId, "Flight plan id is required");
        Preconditions.nonNull(flightID, "Flight id is required");
        Preconditions.nonNull(type, "Flight type is required");
        Preconditions.nonNull(route, "Route is required");
        Preconditions.nonNull(aircraft, "Aircraft is required");
        Preconditions.nonNull(pilot, "Pilot is required");
        Preconditions.nonEmpty(flightLegs, "Flight plan must contain at least one leg");

        this.flightPlanId = flightPlanId;
        this.extractedflightID = flightID;
        this.flightType = type;
        this.route = route;
        this.legs = new ArrayList<>(flightLegs);
        this.createdAt = Date.today();
        this.aircraft = aircraft;
        this.pilot = pilot;
        this.status = FlightPlanStatus.DRAFT;
    }

    @Override
    public FlightPlanId identity(){
        return this.flightPlanId;
    }

    public boolean sameAs(Object other){
        if (!(other instanceof FlightPlan)) return false;
        FlightPlan other1 = (FlightPlan) other;
        if (this == other1) return true;
        return this.flightPlanId.equals(other1.flightPlanId);
    }

    public FlightPlanStatus status() {
        return status;
    }

    public FlightId flightId() {
        return extractedflightID;
    }

    public FlightType flightType() {
        return flightType;
    }

    public FlightRoute route() {
        return route;
    }

    public Aircraft aircraft() {
        return aircraft;
    }

    public Pilot pilot() {
        return pilot;
    }

    public List<WeatherData> weatherData() {
        return Collections.unmodifiableList(weatherData);
    }

    public List<FlightLeg> legs() {
        return Collections.unmodifiableList(legs);
    }

    public String dslSourcePath() {
        return dslSourcePath;
    }

    public void updateDslSourcePath(final String path) {
        this.dslSourcePath = path;
    }

    public void validate(){
        if (status != FlightPlanStatus.DRAFT) {
            throw new IllegalStateException("Only draft flight plans can be validated.");
        }
        this.status = FlightPlanStatus.VALIDATED;
    }

    public void reject(){
        if (status != FlightPlanStatus.DRAFT) {
            throw new IllegalStateException("Only draft flight plans can be rejected.");
        }
        this.status = FlightPlanStatus.REJECTED;
    }

    /**
     * Associa uma medicao meteorologica a este plano de voo.
     *
     * <p>O aggregate impede associacoes duplicadas da mesma entidade. Adicionar
     * nova meteorologia a um plano {@link FlightPlanStatus#VALIDATED} ou
     * {@link FlightPlanStatus#REJECTED} invalida esse resultado anterior, por
     * isso o estado volta a {@link FlightPlanStatus#DRAFT} e o plano tem de ser
     * testado novamente.</p>
     *
     * @param weatherData entidade meteorologica existente a associar
     * @return {@code true} apenas quando um teste anterior foi invalidado
     */
    public boolean associateWeatherData(WeatherData weatherData) {
        Preconditions.nonNull(weatherData, "Weather data is required");
        if (this.weatherData.contains(weatherData)) {
            return false;
        }
        final boolean previousTestInvalidated = status != FlightPlanStatus.DRAFT;
        this.weatherData.add(weatherData);
        if (previousTestInvalidated) {
            this.status = FlightPlanStatus.DRAFT;
        }
        return previousTestInvalidated;
    }

}
