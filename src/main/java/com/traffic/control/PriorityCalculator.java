package com.traffic.control;

public class PriorityCalculator {

    public double calculatePriorityScore(DirectionTraffic trafficData) {
        int waitingTime = (int) trafficData.getWaitingTime();
        int vehicleCount = (int) trafficData.getVehicleCount();

        return waitingTime + vehicleCount + 0.5 * waitingTime;

    }

}
