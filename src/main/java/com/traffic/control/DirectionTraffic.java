package com.traffic.control;

public class DirectionTraffic {
    private final int vehicleCount;
    private final int emergencyVehicleCount;
    private final long waitingTime;

    public DirectionTraffic(int vehicleCount, int emergencyVehicleCount, long waitingTime) {

        if (vehicleCount < 0 || emergencyVehicleCount < 0) {
            throw new IllegalArgumentException("Vehicle count and emergency vehicle count cannot be negative");
        }
        if (emergencyVehicleCount > vehicleCount) {
            throw new IllegalArgumentException("Emergency vehicle count cannot be greater than vehicle count");
        }
        if (waitingTime < 0) {
            throw new IllegalArgumentException("Waiting time cannot be negative");
        }
        this.vehicleCount = vehicleCount;
        this.emergencyVehicleCount = emergencyVehicleCount;
        this.waitingTime = waitingTime;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public int getEmergencyVehicleCount() {
        return emergencyVehicleCount;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

}
