
package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.control.Direction;
import com.traffic.control.SignalState;

public class VehicleMovementEvent extends SimulationEvent {

    private final String roadId;
    private final String laneId;
    private final Direction approachDirection;
    private final MovementType movementType;

    // Backward-compatible constructor.
    public VehicleMovementEvent(
            String laneId,
            Direction approachDirection,
            MovementType movementType,
            double eventTime,
            int eventPriority) {

        this(null, laneId, approachDirection, movementType,
             eventTime, eventPriority);
    }

    // Preferred constructor: identifies both road and lane.
    public VehicleMovementEvent(
            String roadId,
            String laneId,
            Direction approachDirection,
            MovementType movementType,
            double eventTime,
            int eventPriority) {

        super(eventTime, "Vehicle_Movement", eventPriority);

        if (roadId != null && roadId.isBlank()) {
            throw new IllegalArgumentException(
                "Road ID cannot be empty"
            );
        }

        if (laneId == null || laneId.isBlank()) {
            throw new IllegalArgumentException(
                "Lane ID cannot be null or empty"
            );
        }

        if (approachDirection == null) {
            throw new IllegalArgumentException(
                "Approach direction cannot be null"
            );
        }

        if (movementType == null) {
            throw new IllegalArgumentException(
                "Movement type cannot be null"
            );
        }

        this.roadId = roadId;
        this.laneId = laneId;
        this.approachDirection = approachDirection;
        this.movementType = movementType;
    }

    public String getRoadId() {
        return roadId;
    }

    public String getLaneId() {
        return laneId;
    }

    public Direction getApproachDirection() {
        return approachDirection;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    @Override
    public void executeEvent(SimulationContext context) {

        if (context == null) {
            throw new IllegalArgumentException(
                "SimulationContext cannot be null"
            );
        }

        Lane lane = roadId == null
            ? context.getLane(laneId)
            : context.getLane(roadId, laneId);

        // Release the pending marker as soon as this event starts.
        context.markMovementEventStarted(lane);

        Road road = context.getRoadForLane(lane);

        if (road.getDirection() != approachDirection) {
            throw new IllegalStateException(
                "Movement direction does not match road direction for "
                + road.getId()
            );
        }

        if (lane.isEmpty()) {
            System.out.println(
                "[Movement SKIPPED] Lane " + laneId + " is empty."
            );
            return;
        }

        // Temporary conditions: preserve the vehicle and wait for a trigger.
        if (road.isBlocked() || lane.isBlocked()) {
            System.out.println(
                "[Movement PAUSED] Road or lane is blocked: "
                + road.getId() + "/" + laneId
            );
            return;
        }

        if (context.isSignalFailed(approachDirection)) {
            System.out.println(
                "[Movement PAUSED] Signal " + approachDirection
                + " has failed."
            );
            return;
        }

        if (context.getTrafficSignal(approachDirection)
                .getCurrentState() != SignalState.GREEN) {

            System.out.println(
                "[Movement PAUSED] Signal " + approachDirection
                + " is not GREEN."
            );
            return;
        }

        Vehicle vehicle = lane.getNextVehicle();

        // These are configuration or assignment errors, not temporary waits.
        if (vehicle.getIntendedMovement() != movementType) {
            throw new IllegalStateException(
                "Vehicle " + vehicle.getId()
                + " intends " + vehicle.getIntendedMovement()
                + " but movement event requests " + movementType
            );
        }

        if (!lane.isMovementPermitted(vehicle.getIntendedMovement())) {
            throw new IllegalStateException(
                "Vehicle " + vehicle.getId()
                + " is in a lane that does not permit "
                + vehicle.getIntendedMovement()
            );
        }

        // Movement succeeds: remove vehicle from its source lane.
        lane.removeNextVehicle();

        Direction exitDirection =
            movementType.getExitDirection(approachDirection);

        // Schedule departure. Completion is recorded by VehicleDepartureEvent.
        context.scheduleEvent(
            new VehicleDepartureEvent(
                vehicle,
                exitDirection,
                getEventTime(),
                getEventPriority()
            )
        );

        System.out.println(
            "[Movement SUCCESS] Vehicle " + vehicle.getId()
            + " moved from " + approachDirection
            + " towards " + exitDirection
            + " at simulation time " + getEventTime()
        );

        // Schedule the next vehicle using the configured headway.
        context.scheduleNextVehicle(
            road.getId(),
            lane.getId(),
            getEventTime(),
            getEventPriority()
        );
    }
}
