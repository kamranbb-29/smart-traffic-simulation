
package com.traffic.statistics;

public class StrategyResult {

    private final String strategyName;
    private final int completedVehicles;
    private final double averageWaitingTime;
    private final double maximumWaitingTime;
    private final double throughput;
    private final double averageQueueLength;

    public StrategyResult(String strategyName, TrafficStatistics stats) {

        if (strategyName == null || strategyName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Strategy name cannot be empty"
            );
        }

        if (stats == null) {
            throw new IllegalArgumentException(
                "Statistics cannot be null"
            );
        }

        this.strategyName = strategyName;
        this.completedVehicles = stats.getCompletedVehicles();
        this.averageWaitingTime = stats.getAverageWaitingTime();
        this.maximumWaitingTime = stats.getMaximumWaitingTime();
        this.throughput = stats.getThroughput();
        this.averageQueueLength = stats.getAverageQueueLength();
    }

    public String getStrategyName() {
        return strategyName;
    }

    public int getCompletedVehicles() {
        return completedVehicles;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public double getMaximumWaitingTime() {
        return maximumWaitingTime;
    }

    public double getThroughput() {
        return throughput;
    }

    public double getAverageQueueLength() {
        return averageQueueLength;
    }
}
