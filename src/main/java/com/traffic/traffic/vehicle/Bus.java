package com.traffic.traffic.vehicle;

public class Bus extends Vehicle {

    public Bus(String id, double arrivalTime) {
        super(id, arrivalTime);
    }

    @Override
    public String getType() {
        return "Bus";
    }
    
}
