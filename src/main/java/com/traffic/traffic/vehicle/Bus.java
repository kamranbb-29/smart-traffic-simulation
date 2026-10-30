package com.traffic.traffic.vehicle;

import com.traffic.simulation.events.MovementType;

public class Bus extends Vehicle {

    public Bus(String id, double arrivalTime){
        super(id, arrivalTime);
    }

    public Bus(String id, double arrivalTime, MovementType intendedMovement) {
        super(id, arrivalTime, intendedMovement);
    }

    @Override
    public String getType() {
        return "Bus";
    }
    
}
