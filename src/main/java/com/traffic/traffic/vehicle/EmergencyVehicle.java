package com.traffic.traffic.vehicle;

public class EmergencyVehicle extends Vehicle {

    public EmergencyVehicle(String id, double arrivalTime) {
        super(id, arrivalTime);
    }

    @Override
    public String getType() {
        return "Emergency Vehicle";
    }
}
