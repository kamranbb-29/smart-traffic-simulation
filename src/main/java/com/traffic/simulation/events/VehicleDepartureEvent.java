
package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.control.Direction;

public class VehicleDepartureEvent extends SimulationEvent {

    private final Vehicle vehicle;
    private final Direction exitDirection;

    public VehicleDepartureEvent(
            Vehicle vehicle,
            Direction exitDirection,
            double eventTime,
            int eventPriority) {

        super(eventTime, "Vehicle_Departure", eventPriority);

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        if (exitDirection == null) {
            throw new IllegalArgumentException("Exit direction cannot be null");
        }

        this.vehicle = vehicle;
        this.exitDirection = exitDirection;
    }

    public String getVehicleId() {
        return vehicle.getId();
    }

    public Direction getExitDirection() {
        return exitDirection;
    }

    @Override
    public void executeEvent(SimulationContext context) {

        if (context == null) {
            throw new IllegalArgumentException(
                "SimulationContext cannot be null"
            );
        }

        context.recordCompletedVehicle(vehicle);

        System.out.println(
            "Vehicle " + vehicle.getId()
            + " departed through " + exitDirection
            + " at simulation time " + getEventTime()
        );
    }
}
