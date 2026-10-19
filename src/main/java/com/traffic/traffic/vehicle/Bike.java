package com.traffic.traffic.vehicle;

public class Bike extends Vehicle {

    public Bike(String id, double arrivalTime) {
        super(id, arrivalTime);
    }

    @Override
    public String getType() {
        return "Bike";
    }
}
