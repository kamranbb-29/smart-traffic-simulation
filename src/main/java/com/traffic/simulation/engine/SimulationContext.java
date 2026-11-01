package com.traffic.simulation.engine;

import com.traffic.traffic.road.Lane;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Road;
import com.traffic.control.*;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.simulation.scheduler.EventScheduling;
import com.traffic.simulation.events.MovementType;
import com.traffic.simulation.events.SimulationEvent;
import com.traffic.simulation.events.VehicleMovementEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.EnumMap;
import java.util.OptionalDouble;

public class SimulationContext {
    private final SimulationClock clock;
    private final Intersection intersection;
    private final Map<Direction, TrafficSignal> trafficSignals;
    private final Set<Direction> failedSignals;
    private final List<Vehicle> completedVehicles;
    private final Set<Vehicle> completedVehicleSet;
    private final EventScheduling eventScheduling;
    private static final double DEFAULT_VEHICLE_HEADWAY = 1.0;

    private final Set<Lane> scheduledMovementLanes;
    private final double vehicleHeadway;

    private int successfulMovementCount = 0;
    private double lastSuccessfulMovementTime = Double.NaN;
    private boolean phaseBudgetExhausted = false;
    private SimulationTerminationStatus terminationStatus = null;

    private final Map<String, Double> movementTimestamps;
    private final Map<String, Double> departureTimestamps;
    private final Map<Direction, List<GreenInterval>> greenIntervals;

    public SimulationContext(SimulationClock clock, Intersection intersection) {
        this(clock, intersection, null, DEFAULT_VEHICLE_HEADWAY);
    }

    public SimulationContext(SimulationClock clock, Intersection intersection, EventScheduling eventScheduling) {
        this(clock, intersection, eventScheduling, DEFAULT_VEHICLE_HEADWAY);
    }

    public SimulationContext(SimulationClock clock, Intersection intersection, EventScheduling eventScheduling,
            double vehicleHeadway) {

        if (clock == null) {
            throw new IllegalArgumentException(
                    "SimulationClock cannot be null");
        }

        if (intersection == null) {
            throw new IllegalArgumentException(
                    "Intersection cannot be null");
        }

        if (!Double.isFinite(vehicleHeadway) || vehicleHeadway <= 0) {
            throw new IllegalArgumentException(
                    "Vehicle headway must be finite and greater than zero");
        }

        this.clock = clock;
        this.intersection = intersection;
        this.eventScheduling = eventScheduling;
        this.vehicleHeadway = vehicleHeadway;

        this.trafficSignals = new HashMap<>();
        this.failedSignals = new HashSet<>();
        this.completedVehicles = new ArrayList<>();
        this.scheduledMovementLanes = new HashSet<>();
        this.completedVehicleSet = new HashSet<>();

        this.movementTimestamps = new HashMap<>();
        this.departureTimestamps = new HashMap<>();
        this.greenIntervals = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            this.greenIntervals.put(d, new ArrayList<>());
        }
    }

    public SimulationClock getClock() {
        return clock;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public Lane getLane(String laneId) {
        if (laneId == null || laneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lane ID cannot be null or empty");
        }
        if (intersection != null) {
            for (Road road : intersection.getRoads()) {
                for (Lane lane : road.getLanes()) {
                    if (lane.getId().equals(laneId)) {
                        return lane;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Lane with id " + laneId + " does not exist");
    }

    public Lane getLane(String roadId, String laneId) {

        if (roadId == null || roadId.isBlank()) {
            throw new IllegalArgumentException(
                    "Road ID cannot be null or empty");
        }

        if (laneId == null || laneId.isBlank()) {
            throw new IllegalArgumentException(
                    "Lane ID cannot be null or empty");
        }

        Road road = getRoad(roadId);

        for (Lane lane : road.getLanes()) {
            if (lane.getId().equals(laneId)) {
                return lane;
            }
        }

        throw new IllegalArgumentException(
                "Lane " + laneId + " does not exist on road " + roadId);
    }

    public double getVehicleHeadway() {
        return vehicleHeadway;
    }

    public Road getRoad(String roadId) {
        if (roadId == null || roadId.trim().isEmpty()) {
            throw new IllegalArgumentException("Road id cannot be empty");
        }

        if (intersection != null) {
            for (Road road : intersection.getRoads()) {
                if (road.getId().equals(roadId)) {
                    return road;
                }
            }
        }

        throw new IllegalArgumentException("Road with ID " + roadId + " does not exist");
    }

    public void addTrafficSignal(TrafficSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("TrafficSignal cannot be null");
        }

        Direction direction = signal.getDirection();

        if (direction == null) {
            throw new IllegalArgumentException("Signal direction cannot be null");
        }

        if (trafficSignals.containsKey(direction)) {
            throw new IllegalArgumentException(
                    "Traffic signal already exists for " + direction);
        }

        trafficSignals.put(direction, signal);
    }

    public TrafficSignal getTrafficSignal(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }

        TrafficSignal signal = trafficSignals.get(direction);

        if (signal == null) {
            throw new IllegalArgumentException(
                    "No traffic signal found for " + direction);
        }

        return signal;
    }

    public void failSignal(Direction direction) {
        getTrafficSignal(direction);
        failedSignals.add(direction);
        List<GreenInterval> intervals = greenIntervals.get(direction);
        if (intervals != null && !intervals.isEmpty()) {
            GreenInterval last = intervals.get(intervals.size() - 1);
            if (last.isOpen()) {
                last.close(clock.getCurrentTime());
            }
        }
    }

    public void recoverSignal(Direction direction) {
        getTrafficSignal(direction);
        failedSignals.remove(direction);
    }

    public boolean isSignalFailed(Direction direction) {
        getTrafficSignal(direction);
        return failedSignals.contains(direction);
    }

    public void recordCompletedVehicle(Vehicle vehicle) {
        recordCompletedVehicle(vehicle, clock.getCurrentTime());
    }

    public void recordCompletedVehicle(Vehicle vehicle, double departureTime) {
        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle cannot be null");
        }
        validateEventTime(departureTime);

        if (completedVehicleSet.add(vehicle)) {
            completedVehicles.add(vehicle);
            departureTimestamps.put(vehicle.getId(), departureTime);
        }
    }

    public Double getDepartureTime(String vehicleId) {
        return departureTimestamps.get(vehicleId);
    }

    public Map<String, Double> getDepartureTimestamps() {
        return Collections.unmodifiableMap(departureTimestamps);
    }

    public List<Vehicle> getCompletedVehicles() {
        return List.copyOf(completedVehicles);
    }

    public int getCompletedVehicleCount() {
        return completedVehicles.size();
    }

    public void scheduleEvent(SimulationEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "SimulationEvent cannot be null");
        }

        if (eventScheduling == null) {
            throw new IllegalStateException(
                    "Event scheduling is not available in this context");
        }

        eventScheduling.scheduleEvent(event);
    }

    // Schedules a movement attempt for the first vehicle in a lane.
    public boolean scheduleMovementForLane(
            String roadId,
            String laneId,
            double eventTime,
            int eventPriority) {

        validateEventTime(eventTime);

        Road road = getRoad(roadId);
        Lane lane = getLane(roadId, laneId);

        if (road.isBlocked() || lane.isBlocked() || lane.isEmpty()) {
            return false;
        }

        Direction direction = road.getDirection();

        if (direction == null) {
            throw new IllegalStateException(
                    "Road " + roadId + " has no direction assigned");
        }

        TrafficSignal signal = getTrafficSignal(direction);

        if (failedSignals.contains(direction)
                || signal.getCurrentState() != SignalState.GREEN) {
            return false;
        }

        Vehicle vehicle = lane.getNextVehicle();
        MovementType movement = vehicle.getIntendedMovement();

        // An incompatible vehicle indicates an assignment/configuration bug.
        if (!lane.isMovementPermitted(movement)) {
            throw new IllegalStateException(
                    "Vehicle " + vehicle.getId()
                            + " is in incompatible lane " + laneId
                            + " on road " + roadId);
        }

        // Only one pending movement event per lane.
        if (!scheduledMovementLanes.add(lane)) {
            return false;
        }

        try {
            scheduleEvent(
                    new VehicleMovementEvent(
                            roadId,
                            laneId,
                            direction,
                            movement,
                            eventTime,
                            eventPriority));
        } catch (RuntimeException exception) {
            scheduledMovementLanes.remove(lane);
            throw exception;
        }

        return true;
    }

    // Schedules eligible lanes belonging to one approach direction.
    public void scheduleMovementsForDirection(
            Direction direction,
            double eventTime,
            int eventPriority) {

        if (direction == null) {
            throw new IllegalArgumentException(
                    "Direction cannot be null");
        }

        validateEventTime(eventTime);

        for (Road road : intersection.getRoads()) {
            if (road.getDirection() != direction) {
                continue;
            }

            for (Lane lane : road.getLanes()) {
                scheduleMovementForLane(
                        road.getId(),
                        lane.getId(),
                        eventTime,
                        eventPriority);
            }
        }
    }

    // Schedules eligible lanes belonging to one road.
    public void scheduleMovementsForRoad(
            String roadId,
            double eventTime,
            int eventPriority) {

        validateEventTime(eventTime);

        Road road = getRoad(roadId);

        for (Lane lane : road.getLanes()) {
            scheduleMovementForLane(
                    roadId,
                    lane.getId(),
                    eventTime,
                    eventPriority);
        }
    }

    // Releases the pending marker when a movement event starts.
    public void markMovementEventStarted(Lane lane) {

        if (lane == null) {
            throw new IllegalArgumentException(
                    "Lane cannot be null");
        }

        scheduledMovementLanes.remove(lane);
    }

    // Schedules the next vehicle after a successful movement.
    public boolean scheduleNextVehicle(
            String roadId,
            String laneId,
            double successfulMovementTime,
            int eventPriority) {

        validateEventTime(successfulMovementTime);

        double nextMovementTime = successfulMovementTime + vehicleHeadway;

        if (!Double.isFinite(nextMovementTime)) {
            throw new IllegalArgumentException(
                    "Next movement time exceeds the supported range");
        }

        return scheduleMovementForLane(
                roadId,
                laneId,
                nextMovementTime,
                eventPriority);
    }

    // Clears pending movement markers during a simulation reset.
    public void clearScheduledMovementEvents() {
        scheduledMovementLanes.clear();
    }

    private void validateEventTime(double eventTime) {

        if (!Double.isFinite(eventTime) || eventTime < 0) {
            throw new IllegalArgumentException(
                    "Event time must be finite and non-negative");
        }
    }

    public Road getRoadForLane(Lane targetLane) {
        if (targetLane == null) {
            throw new IllegalArgumentException(
                    "Lane cannot be null");
        }

        for (Road road : intersection.getRoads()) {
            for (Lane lane : road.getLanes()) {
                if (lane == targetLane) {
                    return road;
                }
            }
        }

        throw new IllegalArgumentException(
                "Lane does not belong to this intersection");
    }

    public boolean hasPendingArrivalEvents() {
        if (eventScheduling == null) {
            throw new IllegalStateException("EventScheduling is not configured in this context");
        }
        return eventScheduling.hasPendingArrivalEvents();
    }

    public OptionalDouble getNextArrivalEventTime() {
        if (eventScheduling == null) {
            throw new IllegalStateException("EventScheduling is not configured in this context");
        }
        return eventScheduling.getNextArrivalEventTime();
    }

    public void recordSuccessfulMovement(double movementTime) {
        recordSuccessfulMovement(null, movementTime);
    }

    public void recordSuccessfulMovement(Vehicle vehicle, double movementTime) {
        validateEventTime(movementTime);
        this.successfulMovementCount++;
        this.lastSuccessfulMovementTime = movementTime;
        if (vehicle != null) {
            movementTimestamps.put(vehicle.getId(), movementTime);
        }
    }

    public Double getMovementTime(String vehicleId) {
        return movementTimestamps.get(vehicleId);
    }

    public Map<String, Double> getMovementTimestamps() {
        return Collections.unmodifiableMap(movementTimestamps);
    }

    public void recordSignalStateChange(Direction direction, SignalState state, double timestamp) {
        if (direction == null || state == null) {
            throw new IllegalArgumentException("Direction and SignalState cannot be null");
        }
        validateEventTime(timestamp);

        List<GreenInterval> intervals = greenIntervals.computeIfAbsent(direction, k -> new ArrayList<>());

        if (state == SignalState.GREEN) {
            if (intervals.isEmpty() || !intervals.get(intervals.size() - 1).isOpen()) {
                intervals.add(new GreenInterval(timestamp));
            }
        } else {
            if (!intervals.isEmpty()) {
                GreenInterval last = intervals.get(intervals.size() - 1);
                if (last.isOpen()) {
                    last.close(timestamp);
                }
            }
        }
    }

    public List<GreenInterval> getGreenIntervals(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }
        return Collections.unmodifiableList(greenIntervals.getOrDefault(direction, Collections.emptyList()));
    }

    public Map<Direction, List<GreenInterval>> getAllGreenIntervals() {
        Map<Direction, List<GreenInterval>> copy = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            copy.put(d, getGreenIntervals(d));
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Closes all currently open GREEN intervals across all directions at the specified
     * termination timestamp. Already-closed intervals are untouched.
     * Repeated finalization is safe and idempotent.
     * Does not simulate a physical signal transition; this is measurement-window finalization only.
     */
    public void closeActiveGreenIntervals(double terminationTimestamp) {
        validateEventTime(terminationTimestamp);
        for (List<GreenInterval> intervals : greenIntervals.values()) {
            if (!intervals.isEmpty()) {
                GreenInterval last = intervals.get(intervals.size() - 1);
                if (last.isOpen()) {
                    double closeTime = Math.max(last.getStartTime(), terminationTimestamp);
                    last.close(closeTime);
                }
            }
        }
    }

    public OptionalDouble getEmergencyGrantDelay(Direction direction, double arrivalTime) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }
        validateEventTime(arrivalTime);

        List<GreenInterval> intervals = greenIntervals.getOrDefault(direction, Collections.emptyList());

        // 1. If its direction is already GREEN at arrival, grant delay is zero.
        for (GreenInterval interval : intervals) {
            if (interval.isActiveAt(arrivalTime)) {
                return OptionalDouble.of(0.0);
            }
        }

        // 2. Otherwise, use the first GREEN interval starting after arrival.
        for (GreenInterval interval : intervals) {
            if (interval.startsAfter(arrivalTime)) {
                return OptionalDouble.of(interval.getStartTime() - arrivalTime);
            }
        }

        // 3. If no qualifying GREEN occurs before termination, represent as unavailable/censored
        return OptionalDouble.empty();
    }

    public int getPendingArrivalCount() {
        if (eventScheduling == null) {
            throw new IllegalStateException("EventScheduling is not configured in this context");
        }
        return eventScheduling.getPendingArrivalCount();
    }

    public int getPendingDepartureCount() {
        if (eventScheduling == null) {
            throw new IllegalStateException("EventScheduling is not configured in this context");
        }
        return eventScheduling.getPendingDepartureCount();
    }

    public List<com.traffic.simulation.events.VehicleDepartureEvent> getPendingDepartureEvents() {
        if (eventScheduling instanceof com.traffic.simulation.scheduler.EventScheduler scheduler) {
            return scheduler.getPendingDepartureEvents();
        }
        return Collections.emptyList();
    }

    public int getSuccessfulMovementCount() {
        return successfulMovementCount;
    }

    public OptionalDouble getLastSuccessfulMovementTime() {
        return Double.isNaN(lastSuccessfulMovementTime)
                ? OptionalDouble.empty()
                : OptionalDouble.of(lastSuccessfulMovementTime);
    }

    public boolean isPhaseBudgetExhausted() {
        return phaseBudgetExhausted;
    }

    public void setPhaseBudgetExhausted(boolean phaseBudgetExhausted) {
        this.phaseBudgetExhausted = phaseBudgetExhausted;
    }

    public SimulationTerminationStatus getTerminationStatus() {
        return terminationStatus;
    }

    public void setTerminationStatus(SimulationTerminationStatus terminationStatus) {
        this.terminationStatus = terminationStatus;
    }

    public void resetProgress() {
        this.successfulMovementCount = 0;
        this.lastSuccessfulMovementTime = Double.NaN;
        this.phaseBudgetExhausted = false;
        this.terminationStatus = null;
        this.movementTimestamps.clear();
        this.departureTimestamps.clear();
        for (List<GreenInterval> list : greenIntervals.values()) {
            list.clear();
        }
    }

    public boolean hasPendingRecoveryEvents() {
        if (eventScheduling == null) {
            throw new IllegalStateException("EventScheduling is not configured in this context");
        }
        return eventScheduling.hasPendingRecoveryEvents();
    }

    public boolean hasPendingDepartureEvents() {
        if (eventScheduling == null) {
            throw new IllegalStateException("EventScheduling is not configured in this context");
        }
        return eventScheduling.hasPendingDepartureEvents();
    }

    public boolean hasPendingProgressEvents() {
        if (eventScheduling == null) {
            throw new IllegalStateException("EventScheduling is not configured in this context");
        }
        return eventScheduling.hasPendingProgressEvents();
    }

    public boolean areAllVehiclesBlocked() {
        int vehicleCount = intersection.getVehicleCount();
        if (vehicleCount == 0) {
            return false;
        }

        for (Road road : intersection.getRoads()) {
            boolean roadBlocked = road.isBlocked();
            Direction dir = road.getDirection();
            boolean signalFailed = dir != null && isSignalFailed(dir);

            for (Lane lane : road.getLanes()) {
                if (!lane.isEmpty()) {
                    boolean laneBlocked = roadBlocked || lane.isBlocked() || signalFailed;
                    if (!laneBlocked) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
