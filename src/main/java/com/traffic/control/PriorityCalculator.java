package com.traffic.control;

public class PriorityCalculator {

    public double calculatePriorityScore(DirectionTraffic trafficData) {
        double waitingTime = trafficData.getTotalWaitingTime();
        int vehicleCount = trafficData.getVehicleCount();

        return waitingTime + vehicleCount + 0.5 * waitingTime;

    }

}
