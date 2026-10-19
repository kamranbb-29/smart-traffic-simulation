
package com.traffic.traffic.vehicle;

public abstract class Vehicle {

    private final String id;
    private final double arrivalTime;
    private double waitingTime;

    public Vehicle(String id, double arrivalTime) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID cannot be empty");
        }

        if (!Double.isFinite(arrivalTime) || arrivalTime < 0) {
            throw new IllegalArgumentException(
                "Arrival time must be finite and non-negative"
            );
        }

        this.id = id;
        this.arrivalTime = arrivalTime;
        this.waitingTime = 0.0;
    }

    public String getId() {
        return id;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getWaitingTime() {
        return waitingTime;
    }

    public void addWaitingTime(double time) {
        if (!Double.isFinite(time) || time < 0) {
            throw new IllegalArgumentException(
                "Waiting time increment must be finite and non-negative"
            );
        }

        waitingTime += time;
    }

    public abstract String getType();
}
