package pt.isep.sem4pi.application.service.flightplan;

import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanAltitudeSlotDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanLegDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanSegmentDTO;
import pt.isep.sem4pi.domain.model.flight.FlightLeg;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Arrival;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Date;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Departure;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Time;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelUnit;
import pt.isep.sem4pi.domain.model.flight.segment.AltitudeSlots;
import pt.isep.sem4pi.domain.model.flight.segment.Segment;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.model.route.Wind;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FlightPlanDslPersistenceComparator {

    private static final double COORD_EPSILON = 1e-4;

    private FlightPlanDslPersistenceComparator() {
        // utility
    }

    public static List<String> compare(final FlightPlan plan, final CreateFlightPlanDTO dslPlan) {
        final List<String> errors = new ArrayList<>();

        if (plan == null) {
            errors.add("Flight plan is required.");
            return errors;
        }
        if (dslPlan == null) {
            errors.add("DSL flight plan data is required.");
            return errors;
        }

        if (plan.route() == null) {
            errors.add("Stored flight plan must reference a route.");
            return errors;
        }

        if (!plan.route().identity().code().equalsIgnoreCase(dslPlan.getRouteId())) {
            errors.add("DSL route id does not match the stored flight plan route.");
        }

        if (!plan.flightId().value().equalsIgnoreCase(dslPlan.getFlightId())) {
            errors.add("DSL flight id does not match the stored flight plan.");
        }

        if (!plan.pilot().identity().code().equalsIgnoreCase(dslPlan.getPilotId())) {
            errors.add("DSL pilot id does not match the stored flight plan pilot.");
        }

        if (dslPlan.getAircraftRegistration() != null
                && !dslPlan.getAircraftRegistration().equalsIgnoreCase(plan.aircraft().identity().value())) {
            errors.add("DSL aircraft registration does not match the stored flight plan aircraft.");
        }

        if (dslPlan.getFlightType() != null
                && !plan.flightType().name().equalsIgnoreCase(dslPlan.getFlightType().trim())) {
            errors.add("DSL flight type does not match the stored flight plan.");
        }

        final List<CreateFlightPlanLegDTO> dslLegs = dslPlan.getLegs();
        final List<FlightLeg> storedLegs = plan.legs();
        if (dslLegs == null || dslLegs.isEmpty()) {
            errors.add("DSL flight plan must contain at least one leg.");
            return errors;
        }
        if (storedLegs.size() != dslLegs.size()) {
            errors.add("DSL leg count does not match the stored flight plan.");
            return errors;
        }

        for (int i = 0; i < dslLegs.size(); i++) {
            compareLeg(errors, storedLegs.get(i), dslLegs.get(i), i + 1);
        }

        compareSegmentsAgainstRoute(errors, plan.route(), dslLegs);
        errors.addAll(FlightPlanDomainValidator.validate(plan, plan.route()));
        return errors;
    }

    private static void compareLeg(final List<String> errors,
                                   final FlightLeg storedLeg,
                                   final CreateFlightPlanLegDTO dslLeg,
                                   final int legNumber) {
        final Departure departure = storedLeg.departure();
        final Arrival arrival = storedLeg.arrival();

        if (departure == null || departure.airport() == null) {
            errors.add("Leg " + legNumber + " is missing departure information in the stored flight plan.");
        } else if (!departure.airport().code().equalsIgnoreCase(dslLeg.getDepartureAirportIata())) {
            errors.add("Leg " + legNumber + " departure airport does not match the DSL.");
        }

        if (arrival == null || arrival.airport() == null) {
            errors.add("Leg " + legNumber + " is missing arrival information in the stored flight plan.");
        } else if (!arrival.airport().code().equalsIgnoreCase(dslLeg.getArrivalAirportIata())) {
            errors.add("Leg " + legNumber + " arrival airport does not match the DSL.");
        }

        compareDepartureSchedule(errors, departure, dslLeg, legNumber);
        compareArrivalSchedule(errors, arrival, dslLeg, legNumber);
        compareFuel(errors, storedLeg.fuel(), dslLeg, legNumber);
        comparePersistedSegments(errors, storedLeg, dslLeg, legNumber);
    }

    private static void compareDepartureSchedule(final List<String> errors,
                                                 final Departure departure,
                                                 final CreateFlightPlanLegDTO dslLeg,
                                                 final int legNumber) {
        if (departure == null || departure.date() == null || departure.time() == null) {
            errors.add("Leg " + legNumber + " departure schedule is incomplete in the stored flight plan.");
            return;
        }

        final Date date = departure.date();
        final Time time = departure.time();
        if (date.day() != dslLeg.getDepartureDay()
                || date.month() != dslLeg.getDepartureMonth()
                || date.year() != dslLeg.getDepartureYear()) {
            errors.add("Leg " + legNumber + " departure date does not match the DSL.");
        }
        if (time.hour() != dslLeg.getDepartureHour()
                || time.minute() != dslLeg.getDepartureMinute()
                || time.second() != safeSecond(dslLeg.getDepartureSecond())) {
            errors.add("Leg " + legNumber + " departure time does not match the DSL.");
        }
    }

    private static void compareArrivalSchedule(final List<String> errors,
                                               final Arrival arrival,
                                               final CreateFlightPlanLegDTO dslLeg,
                                               final int legNumber) {
        if (arrival == null || arrival.time() == null) {
            errors.add("Leg " + legNumber + " arrival schedule is incomplete in the stored flight plan.");
            return;
        }

        final Time time = arrival.time();
        if (time.hour() != dslLeg.getArrivalHour()
                || time.minute() != dslLeg.getArrivalMinute()
                || time.second() != safeSecond(dslLeg.getArrivalSecond())) {
            errors.add("Leg " + legNumber + " arrival time does not match the DSL.");
        }
    }

    private static void compareFuel(final List<String> errors,
                                    final Fuel fuel,
                                    final CreateFlightPlanLegDTO dslLeg,
                                    final int legNumber) {
        if (fuel == null || fuel.capacity() == null || fuel.unit() == null) {
            errors.add("Leg " + legNumber + " fuel information is missing in the stored flight plan.");
            return;
        }

        if (Math.abs(fuel.capacity().value() - dslLeg.getFuelQuantity()) > 0.0001) {
            errors.add("Leg " + legNumber + " fuel quantity does not match the DSL.");
        }

        final String storedUnit = fuel.unit().name();
        final String dslUnit = dslLeg.getFuelUnit() == null
                ? ""
                : dslLeg.getFuelUnit().trim().toUpperCase(Locale.ROOT);
        if (!storedUnit.equals(dslUnit)) {
            errors.add("Leg " + legNumber + " fuel unit does not match the DSL.");
        }
    }

    private static void comparePersistedSegments(final List<String> errors,
                                                 final FlightLeg storedLeg,
                                                 final CreateFlightPlanLegDTO dslLeg,
                                                 final int legNumber) {
        final List<Segment> storedSegments = storedLeg.segments();
        final List<CreateFlightPlanSegmentDTO> dslSegments = dslLeg.getSegments();
        if (storedSegments == null || storedSegments.isEmpty()) {
            return;
        }
        if (dslSegments == null || dslSegments.isEmpty()) {
            errors.add("Leg " + legNumber + " segments are missing in the DSL.");
            return;
        }
        if (storedSegments.size() != dslSegments.size()) {
            errors.add("Leg " + legNumber + " segment count does not match the DSL.");
            return;
        }

        for (int i = 0; i < dslSegments.size(); i++) {
            compareSegment(errors, storedSegments.get(i), dslSegments.get(i), legNumber, i + 1);
        }
    }

    private static void compareSegment(final List<String> errors,
                                       final Segment storedSegment,
                                       final CreateFlightPlanSegmentDTO dslSegment,
                                       final int legNumber,
                                       final int segmentNumber) {
        if (!sameCoordinates(storedSegment.startCoordinates(),
                dslSegment.getStartLatitude(), dslSegment.getStartLongitude())) {
            errors.add("Leg " + legNumber + " segment " + segmentNumber + " start coordinates do not match the DSL.");
        }
        if (!sameCoordinates(storedSegment.endCoordinates(),
                dslSegment.getEndLatitude(), dslSegment.getEndLongitude())) {
            errors.add("Leg " + legNumber + " segment " + segmentNumber + " end coordinates do not match the DSL.");
        }

        final List<AltitudeSlots> storedSlots = storedSegment.altitudeSlots();
        final List<CreateFlightPlanAltitudeSlotDTO> dslSlots = dslSegment.getAltitudeSlots();
        if (storedSlots.size() != (dslSlots == null ? 0 : dslSlots.size())) {
            errors.add("Leg " + legNumber + " segment " + segmentNumber + " altitude slot count does not match the DSL.");
            return;
        }
        for (int i = 0; i < storedSlots.size(); i++) {
            final AltitudeSlots storedSlot = storedSlots.get(i);
            final CreateFlightPlanAltitudeSlotDTO dslSlot = dslSlots.get(i);
            if (storedSlot.minimumHeight() != dslSlot.getMinimumAltitude()
                    || storedSlot.maximumHeight() != dslSlot.getMaximumAltitude()) {
                errors.add("Leg " + legNumber + " segment " + segmentNumber
                        + " altitude slot " + (i + 1) + " does not match the DSL.");
            }
        }

        if (storedSegment.windAngle() != null
                && Math.abs(storedSegment.windAngle().angle() - dslSegment.getWindAngle()) > 0.0001) {
            errors.add("Leg " + legNumber + " segment " + segmentNumber + " wind angle does not match the DSL.");
        }
        if (storedSegment.speed() != null
                && Math.abs(storedSegment.speed().value() - dslSegment.getSpeed()) > 0.0001) {
            errors.add("Leg " + legNumber + " segment " + segmentNumber + " speed does not match the DSL.");
        }
    }

    private static void compareSegmentsAgainstRoute(final List<String> errors,
                                                    final FlightRoute route,
                                                    final List<CreateFlightPlanLegDTO> dslLegs) {
        final List<CreateFlightPlanSegmentDTO> dslSegments = new ArrayList<>();
        for (final CreateFlightPlanLegDTO leg : dslLegs) {
            if (leg.getSegments() != null) {
                dslSegments.addAll(leg.getSegments());
            }
        }

        final List<RouteSegment> routeSegments = route.segments();
        if (dslSegments.size() != routeSegments.size()) {
            errors.add("DSL segment count does not match the stored route segment count.");
            return;
        }

        for (int i = 0; i < dslSegments.size(); i++) {
            compareDslSegmentWithRoute(errors, dslSegments.get(i), routeSegments.get(i), i + 1);
        }
    }

    private static void compareDslSegmentWithRoute(final List<String> errors,
                                                   final CreateFlightPlanSegmentDTO dslSegment,
                                                   final RouteSegment routeSegment,
                                                   final int segmentNumber) {
        if (!sameCoordinates(routeSegment.startNode(),
                dslSegment.getStartLatitude(), dslSegment.getStartLongitude())) {
            errors.add("Route segment " + segmentNumber + " start coordinates do not match the DSL.");
        }
        if (!sameCoordinates(routeSegment.endNode(),
                dslSegment.getEndLatitude(), dslSegment.getEndLongitude())) {
            errors.add("Route segment " + segmentNumber + " end coordinates do not match the DSL.");
        }

        final List<AltitudeRange> routeAltitudes = routeSegment.allowedAltitudes();
        final List<CreateFlightPlanAltitudeSlotDTO> dslAltitudes = dslSegment.getAltitudeSlots();
        if (routeAltitudes.size() != (dslAltitudes == null ? 0 : dslAltitudes.size())) {
            errors.add("Route segment " + segmentNumber + " altitude slot count does not match the DSL.");
            return;
        }
        for (int i = 0; i < routeAltitudes.size(); i++) {
            final AltitudeRange routeSlot = routeAltitudes.get(i);
            final CreateFlightPlanAltitudeSlotDTO dslSlot = dslAltitudes.get(i);
            if (!routeSlot.minAltitude().equals(dslSlot.getMinimumAltitude())
                    || !routeSlot.maxAltitude().equals(dslSlot.getMaximumAltitude())) {
                errors.add("Route segment " + segmentNumber + " altitude slot " + (i + 1) + " does not match the DSL.");
            }
        }

        final Wind routeWind = routeSegment.wind();
        if (routeWind != null
                && Math.abs(routeWind.directionAngle() - dslSegment.getWindAngle()) > 0.0001) {
            errors.add("Route segment " + segmentNumber + " wind angle does not match the DSL.");
        }
    }

    private static boolean sameCoordinates(final Coordinates coordinates,
                                           final Double latitude,
                                           final Double longitude) {
        if (coordinates == null || latitude == null || longitude == null) {
            return false;
        }
        return Math.abs(coordinates.getLatitude() - latitude) < COORD_EPSILON
                && Math.abs(coordinates.getLongitude() - longitude) < COORD_EPSILON;
    }

    private static int safeSecond(final Integer second) {
        return second == null ? 0 : second;
    }
}
