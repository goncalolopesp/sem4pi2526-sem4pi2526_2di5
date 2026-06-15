package pt.isep.sem4pi.application.service.flightplan;

import pt.isep.sem4pi.domain.model.flight.FlightLeg;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelCapacity;
import pt.isep.sem4pi.domain.model.route.FlightRoute;

import java.util.ArrayList;
import java.util.List;

public final class FlightPlanDomainValidator {

    private FlightPlanDomainValidator() {
        // utility
    }

    public static List<String> validate(final FlightPlan plan) {
        if (plan == null) {
            return List.of("Flight plan is required.");
        }
        return validate(plan, plan.route());
    }

    public static List<String> validate(final FlightPlan plan, final FlightRoute route) {
        final List<String> errors = new ArrayList<>();

        if (plan == null) {
            errors.add("Flight plan is required.");
            return errors;
        }
        if (route == null) {
            errors.add("Flight plan must reference a route.");
            return errors;
        }

        if (route.segments().isEmpty()) {
            errors.add("Route has no segments.");
        } else {
            for (int i = 0; i < route.segments().size(); i++) {
                if (route.segments().get(i).allowedAltitudes().isEmpty()) {
                    errors.add("Route segment " + (i + 1) + " has no altitude slots.");
                }
            }
        }

        final List<FlightLeg> legs = plan.legs();
        if (legs == null || legs.isEmpty()) {
            errors.add("Flight plan must contain at least one leg.");
            return errors;
        }

        final FlightLeg firstLeg = legs.get(0);
        final FlightLeg lastLeg = legs.get(legs.size() - 1);

        if (firstLeg.departure() == null || firstLeg.departure().airport() == null) {
            errors.add("First leg must have a departure airport.");
        } else if (!route.originAirport().code().equalsIgnoreCase(firstLeg.departure().airport().code())) {
            errors.add("First leg departure must match route origin airport.");
        }

        if (lastLeg.arrival() == null || lastLeg.arrival().airport() == null) {
            errors.add("Last leg must have an arrival airport.");
        } else if (!route.destinationAirport().code().equalsIgnoreCase(lastLeg.arrival().airport().code())) {
            errors.add("Last leg arrival must match route destination airport.");
        }

        for (int i = 0; i < legs.size(); i++) {
            final FlightLeg leg = legs.get(i);
            final Fuel fuel = leg.fuel();
            if (fuel == null || fuel.capacity() == null) {
                errors.add("Leg " + (i + 1) + " must include fuel information.");
                continue;
            }
            final FuelCapacity capacity = fuel.capacity();
            if (capacity.value() == null || capacity.value() <= 0.0) {
                errors.add("Leg " + (i + 1) + " fuel quantity must be strictly positive.");
            }
        }

        return errors;
    }
}
