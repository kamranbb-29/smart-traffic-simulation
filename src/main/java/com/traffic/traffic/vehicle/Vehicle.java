
package com.traffic.traffic.vehicle;

import com.traffic.simulation.events.MovementType;

public abstract class Vehicle {

    private final String id;
    private final double arrivalTime;
    private double waitingTime;
    private boolean waitingTimeFinalized = false;
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

    public double getWaitingTime(double currentSimTime) {
        validateTimestamp(currentSimTime, "Current simulation time");
        if (waitingTimeFinalized) {
            return waitingTime;
        }
        return waitingTime + Math.max(0.0, currentSimTime - arrivalTime);
    }

    public MovementType getIntendedMovement() {
        return intendedMovement;
    }

    /**
     * Compatibility mutator.
     * Assumption: addWaitingTime() represents an externally accumulated baseline delay
     * (e.g. from upstream links or unit test fixtures) because the repository does not
     * establish its historical semantics. It does not overlap with the dynamic queue-wait interval.
     */
    public void addWaitingTime(double time) {
        validateTimestamp(time, "Waiting time increment");
        if (waitingTimeFinalized) {
            throw new IllegalStateException("Cannot add waiting time after waiting time has been finalized");
        }
        waitingTime += time;
    }

    /**
     * Finalizes the queue waiting time exactly once upon successful movement.
     * Timestamps are validated before checking the finalization guard.
     */
    public void finalizeWaitingTime(double movementTime) {
        validateTimestamp(movementTime, "Movement time");

        if (movementTime < arrivalTime) {
            throw new IllegalArgumentException(
                "Movement time (" + movementTime + ") cannot be earlier than arrival time (" + arrivalTime + ")"
            );
        }

        if (waitingTimeFinalized) {
            return; // Idempotent guard: never double-count
        }

        waitingTime += (movementTime - arrivalTime);
        waitingTimeFinalized = true;
    }

    public boolean isWaitingTimeFinalized() {
        return waitingTimeFinalized;
    }

    private void validateTimestamp(double time, String name) {
        if (!Double.isFinite(time) || time < 0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative: " + time);
        }
    }

    public abstract String getType();
}
