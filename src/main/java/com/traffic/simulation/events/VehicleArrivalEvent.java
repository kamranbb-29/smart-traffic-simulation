
package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Vehicle;

public class VehicleArrivalEvent extends SimulationEvent {

    private final Vehicle vehicle;
    private final String roadId;

    public VehicleArrivalEvent(
            Vehicle vehicle,
            String roadId,
            double eventTime,
            int eventPriority) {

        super(eventTime, "Vehicle_Arrival", eventPriority);

        if (vehicle == null) {
            throw new IllegalArgumentException(
                "Vehicle cannot be null"
            );
        }

        if (roadId == null || roadId.isBlank()) {
            throw new IllegalArgumentException(
                "Road ID cannot be null or empty"
            );
        }

        this.vehicle = vehicle;
        this.roadId = roadId;
    }

    public String getVehicleId() {
        return vehicle.getId();
    }

    public String getRoadId() {
        return roadId;
    }

    @Override
    public void executeEvent(SimulationContext context) {

        if (context == null) {
            throw new IllegalArgumentException(
                "SimulationContext cannot be null"
            );
        }

        Road road = context.getRoad(roadId);

        Lane selectedLane = road
            .findLeastLoadedCompatibleLane(
                vehicle.getIntendedMovement()
            )
            .orElseThrow(() ->
                new IllegalStateException(
                    "No compatible lane found on road " + roadId
                    + " for vehicle " + vehicle.getId()
                    + " intending " + vehicle.getIntendedMovement()
                )
            );

        selectedLane.addVehicle(vehicle);
        context.scheduleMovementForLane(
            roadId,
            selectedLane.getId(),
            getEventTime(),
            getEventPriority()
        );

        System.out.println(
            "Vehicle " + vehicle.getId()
            + " arrived on road " + roadId
            + " and was assigned to lane " + selectedLane.getId()
            + " at simulation time " + getEventTime()
        );
    }
}
