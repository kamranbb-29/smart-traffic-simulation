package com.traffic.statistics;

public class TrafficStatistics {

    private double totalWaitingTime;
    private int completedVehicles;
    private double maximumWaitingTime;
    private double simulationDuration;
    
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
public void setSimulationDuration(double simulationDuration) {
    if (simulationDuration < 0) {
        throw new IllegalArgumentException(
            "Simulation duration cannot be negative"
        );
    }

    this.simulationDuration = simulationDuration;
}

public double getThroughput() {
    if (simulationDuration == 0) {
        return 0.0;
    }

    return completedVehicles / simulationDuration;
}
}