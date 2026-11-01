package com.traffic.control;

public class DirectionTraffic {
    private final int vehicleCount;
    private final int emergencyVehicleCount;
    private final double waitingTime;
    private final double maxWaitingTime;

    public DirectionTraffic(int vehicleCount, int emergencyVehicleCount, double totalWaitingTime, double maxWaitingTime) {

        if (vehicleCount < 0 || emergencyVehicleCount < 0) {
            throw new IllegalArgumentException("Vehicle count and emergency vehicle count cannot be negative");
        }
        if (emergencyVehicleCount > vehicleCount) {
            throw new IllegalArgumentException("Emergency vehicle count cannot be greater than vehicle count");
        }
        if (!Double.isFinite(totalWaitingTime) || totalWaitingTime < 0) {
            throw new IllegalArgumentException("Total waiting time must be finite and non-negative");
        }
        if (!Double.isFinite(maxWaitingTime) || maxWaitingTime < 0) {
            throw new IllegalArgumentException("Maximum waiting time must be finite and non-negative");
        }
        if (vehicleCount > 0 && maxWaitingTime > (totalWaitingTime + 1e-9)) {
            throw new IllegalArgumentException("Maximum waiting time cannot exceed total waiting time");
        }

        this.vehicleCount = vehicleCount;
        this.emergencyVehicleCount = emergencyVehicleCount;
        this.waitingTime = totalWaitingTime;
        this.maxWaitingTime = maxWaitingTime;
    }

    public DirectionTraffic(int vehicleCount, int emergencyVehicleCount, double waitingTime) {
        this(vehicleCount, emergencyVehicleCount, waitingTime, waitingTime);
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public int getEmergencyVehicleCount() {
        return emergencyVehicleCount;
    }

    public double getWaitingTime() {
        return waitingTime;
    }

    public double getTotalWaitingTime() {
        return waitingTime;
    }

    public double getMaxWaitingTime() {
        return maxWaitingTime;
    }
}
