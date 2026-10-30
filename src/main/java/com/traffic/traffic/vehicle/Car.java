
package com.traffic.traffic.vehicle;

import com.traffic.simulation.events.MovementType;
public class Car extends Vehicle {

    public Car(String id, double arrivalTime){
        super(id, arrivalTime);
    }
    public Car(String id, double arrivalTime, MovementType intendedMovement) {
        super(id, arrivalTime, intendedMovement);
    }

    @Override
    public String getType() {
        return "Car";
    }
}
