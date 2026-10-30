package com.traffic.traffic.vehicle;

import com.traffic.simulation.events.MovementType;

public class Bike extends Vehicle {

    public Bike(String id, double arrivalTime){
        super(id, arrivalTime);
    }
    public Bike(String id, double arrivalTime, MovementType intendedMovement) {
        super(id, arrivalTime, intendedMovement);
    }

    @Override
    public String getType() {
        return "Bike";
    }
}
