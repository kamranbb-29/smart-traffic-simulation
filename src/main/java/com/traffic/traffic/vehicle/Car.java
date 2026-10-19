
package com.traffic.traffic.vehicle;

public class Car extends Vehicle {

    public Car(String id, double arrivalTime) {
        super(id, arrivalTime);
    }

    @Override
    public String getType() {
        return "Car";
    }
}
