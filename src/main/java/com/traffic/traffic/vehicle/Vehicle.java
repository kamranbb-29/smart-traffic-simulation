
package com.traffic.traffic.vehicle;

import com.traffic.simulation.events.MovementType;

public abstract class Vehicle {

    private final String id;
    private final double arrivalTime;
    private double waitingTime;
    private final MovementType intendedMovement;

    public Vehicle(String id, double arrivalTime) {
        this(id, arrivalTime, MovementType.STRAIGHT);
    }

    public Vehicle(String id, double arrivalTime, MovementType intendedMovement) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID cannot be empty");
        }

        if (arrivalTime < 0 || !Double.isFinite(arrivalTime)) {
            throw new IllegalArgumentException("Arrival time must be non-negative");
        }

        if (intendedMovement == null) {
            throw new IllegalArgumentException("Intended movement cannot be null");
        }

        this.id = id;
        this.arrivalTime = arrivalTime;
        this.intendedMovement = intendedMovement;
        this.waitingTime = 0;
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

    public MovementType getIntendedMovement() {
        return intendedMovement;
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
