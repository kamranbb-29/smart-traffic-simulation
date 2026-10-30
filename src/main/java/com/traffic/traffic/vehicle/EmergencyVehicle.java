package com.traffic.traffic.vehicle;

import com.traffic.simulation.events.MovementType;

public class EmergencyVehicle extends Vehicle {

    public EmergencyVehicle(String id, double arrivalTime){
        super(id, arrivalTime);
    }

    public EmergencyVehicle(String id, double arrivalTime, MovementType intendedMovement) {
        super(id, arrivalTime, intendedMovement);
    }

    @Override
    public String getType() {
        return "Emergency Vehicle";
    }
}
