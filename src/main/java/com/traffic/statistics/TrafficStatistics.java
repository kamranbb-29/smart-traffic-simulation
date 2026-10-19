package com.traffic.statistics;

public class TrafficStatistics {

    private double totalWaitingTime;
    private int completedVehicles;
    private double maximumWaitingTime;

    
public void recordCompletedVehicle(double waitingTime) {
    if (waitingTime < 0) {
        throw new IllegalArgumentException(
            "Waiting time cannot be negative"
        );
    }
    if (waitingTime > maximumWaitingTime) {
    maximumWaitingTime = waitingTime;
}
    totalWaitingTime += waitingTime;
    completedVehicles++;
}

public double getAverageWaitingTime() {
    if (completedVehicles == 0) {
        return 0.0;
    }

    return totalWaitingTime / completedVehicles;
}

public int getCompletedVehicles() {
    return completedVehicles;
}

public double getMaximumWaitingTime() {
    return maximumWaitingTime;
}
}