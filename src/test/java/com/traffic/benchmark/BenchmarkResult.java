package com.traffic.benchmark;

import com.traffic.simulation.engine.SimulationTerminationStatus;
import java.util.List;
import java.util.OptionalDouble;

public class BenchmarkResult {

    private final String scenarioName;
    private final String strategyName;
    private final int runIndex;

    private final int totalIntroduced;
    private final int completedCount;
    private final int queuedCount;
    private final int inTransitCount;
    private final int remainingCount;
    private final int pendingArrivalCount;
    private final double completionRate;
    private final boolean accountingValid;
    private final boolean simulationComplete;

    private final double totalSimulatedTime;
    private final double wallClockTimeMs;
    private final double throughput;

    private final double avgTimeInSystem;
    private final double maxTimeInSystem;
    private final double minTimeInSystem;

    private final double avgCompletedWaitingTime;
    private final double maxCompletedWaitingTime;
    private final double avgUnfinishedWaitSoFar;
    private final double maxUnfinishedWaitSoFar;

    private final double avgEmergencyTimeInSystem;
    private final double maxEmergencyTimeInSystem;
    private final double avgEmergencyGrantDelay;
    private final double avgEmergencyServiceDelay;
    private final double emergencyResponseTimeToService;

    private final double reportedInternalAvgWaitingTime;
    private final double reportedInternalMaxWaitingTime;
    private final boolean starvation120sTriggered;
    private final SimulationTerminationStatus terminationStatus;

    private final List<VehicleMetricRecord> vehicleRecords;

    /**
     * Backward-compatible 20-argument constructor.
     */
    public BenchmarkResult(
            String scenarioName,
            String strategyName,
            int runIndex,
            int totalIntroduced,
            int completedCount,
            int remainingCount,
            int pendingArrivalCount,
            double totalSimulatedTime,
            double wallClockTimeMs,
            double throughput,
            double avgTimeInSystem,
            double maxTimeInSystem,
            double minTimeInSystem,
            double avgEmergencyTimeInSystem,
            double maxEmergencyTimeInSystem,
            double emergencyResponseTimeToService,
            double reportedInternalAvgWaitingTime,
            double reportedInternalMaxWaitingTime,
            boolean starvation120sTriggered,
            List<VehicleMetricRecord> vehicleRecords) {

        this(
                scenarioName,
                strategyName,
                runIndex,
                totalIntroduced,
                completedCount,
                remainingCount, // queuedCount
                0, // inTransitCount
                remainingCount,
                pendingArrivalCount,
                totalIntroduced > 0 ? ((double) completedCount / totalIntroduced) : 1.0,
                totalSimulatedTime,
                wallClockTimeMs,
                throughput,
                avgTimeInSystem,
                maxTimeInSystem,
                minTimeInSystem,
                reportedInternalAvgWaitingTime,
                reportedInternalMaxWaitingTime,
                0.0,
                0.0,
                avgEmergencyTimeInSystem,
                maxEmergencyTimeInSystem,
                emergencyResponseTimeToService,
                emergencyResponseTimeToService,
                emergencyResponseTimeToService,
                reportedInternalAvgWaitingTime,
                reportedInternalMaxWaitingTime,
                starvation120sTriggered,
                completedCount == totalIntroduced ? SimulationTerminationStatus.NORMAL_COMPLETION : SimulationTerminationStatus.PHASE_LIMIT_EXHAUSTED,
                vehicleRecords
        );
    }

    /**
     * Comprehensive constructor with detailed accounting and emergency metrics.
     */
    public BenchmarkResult(
            String scenarioName,
            String strategyName,
            int runIndex,
            int totalIntroduced,
            int completedCount,
            int queuedCount,
            int inTransitCount,
            int remainingCount,
            int pendingArrivalCount,
            double completionRate,
            double totalSimulatedTime,
            double wallClockTimeMs,
            double throughput,
            double avgTimeInSystem,
            double maxTimeInSystem,
            double minTimeInSystem,
            double avgCompletedWaitingTime,
            double maxCompletedWaitingTime,
            double avgUnfinishedWaitSoFar,
            double maxUnfinishedWaitSoFar,
            double avgEmergencyTimeInSystem,
            double maxEmergencyTimeInSystem,
            double avgEmergencyGrantDelay,
            double avgEmergencyServiceDelay,
            double emergencyResponseTimeToService,
            double reportedInternalAvgWaitingTime,
            double reportedInternalMaxWaitingTime,
            boolean starvation120sTriggered,
            SimulationTerminationStatus terminationStatus,
            List<VehicleMetricRecord> vehicleRecords) {

        this.scenarioName = scenarioName;
        this.strategyName = strategyName;
        this.runIndex = runIndex;
        this.totalIntroduced = totalIntroduced;
        this.completedCount = completedCount;
        this.queuedCount = queuedCount;
        this.inTransitCount = inTransitCount;
        this.remainingCount = remainingCount;
        this.pendingArrivalCount = pendingArrivalCount;
        this.completionRate = completionRate;
        this.accountingValid = (totalIntroduced == (completedCount + queuedCount + inTransitCount + pendingArrivalCount));
        this.simulationComplete = (completedCount == totalIntroduced);
        this.totalSimulatedTime = totalSimulatedTime;
        this.wallClockTimeMs = wallClockTimeMs;
        this.throughput = throughput;
        this.avgTimeInSystem = avgTimeInSystem;
        this.maxTimeInSystem = maxTimeInSystem;
        this.minTimeInSystem = minTimeInSystem;
        this.avgCompletedWaitingTime = avgCompletedWaitingTime;
        this.maxCompletedWaitingTime = maxCompletedWaitingTime;
        this.avgUnfinishedWaitSoFar = avgUnfinishedWaitSoFar;
        this.maxUnfinishedWaitSoFar = maxUnfinishedWaitSoFar;
        this.avgEmergencyTimeInSystem = avgEmergencyTimeInSystem;
        this.maxEmergencyTimeInSystem = maxEmergencyTimeInSystem;
        this.avgEmergencyGrantDelay = avgEmergencyGrantDelay;
        this.avgEmergencyServiceDelay = avgEmergencyServiceDelay;
        this.emergencyResponseTimeToService = emergencyResponseTimeToService;
        this.reportedInternalAvgWaitingTime = reportedInternalAvgWaitingTime;
        this.reportedInternalMaxWaitingTime = reportedInternalMaxWaitingTime;
        this.starvation120sTriggered = starvation120sTriggered;
        this.terminationStatus = terminationStatus;
        this.vehicleRecords = List.copyOf(vehicleRecords);
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public int getRunIndex() {
        return runIndex;
    }

    public int getTotalIntroduced() {
        return totalIntroduced;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public int getQueuedCount() {
        return queuedCount;
    }

    public int getInTransitCount() {
        return inTransitCount;
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public int getPendingArrivalCount() {
        return pendingArrivalCount;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public boolean isAccountingValid() {
        return accountingValid;
    }

    public boolean isSimulationComplete() {
        return simulationComplete;
    }

    public double getTotalSimulatedTime() {
        return totalSimulatedTime;
    }

    public double getWallClockTimeMs() {
        return wallClockTimeMs;
    }

    public double getThroughput() {
        return throughput;
    }

    public double getAvgTimeInSystem() {
        return avgTimeInSystem;
    }

    public double getMaxTimeInSystem() {
        return maxTimeInSystem;
    }

    public double getMinTimeInSystem() {
        return minTimeInSystem;
    }

    public double getAvgCompletedWaitingTime() {
        return avgCompletedWaitingTime;
    }

    public double getMaxCompletedWaitingTime() {
        return maxCompletedWaitingTime;
    }

    public double getAvgUnfinishedWaitSoFar() {
        return avgUnfinishedWaitSoFar;
    }

    public double getMaxUnfinishedWaitSoFar() {
        return maxUnfinishedWaitSoFar;
    }

    public double getAvgEmergencyTimeInSystem() {
        return avgEmergencyTimeInSystem;
    }

    public double getMaxEmergencyTimeInSystem() {
        return maxEmergencyTimeInSystem;
    }

    public double getAvgEmergencyGrantDelay() {
        return avgEmergencyGrantDelay;
    }

    public double getAvgEmergencyServiceDelay() {
        return avgEmergencyServiceDelay;
    }

    public boolean hasEmergencyGrantDelay() {
        if (vehicleRecords != null && vehicleRecords.stream().anyMatch(v -> v.getEmergencyGrantDelay() != null)) {
            return true;
        }
        return avgEmergencyGrantDelay > 0.0;
    }

    public boolean hasEmergencyServiceDelay() {
        if (vehicleRecords != null && vehicleRecords.stream().anyMatch(v -> v.getEmergencyServiceDelay() != null)) {
            return true;
        }
        return avgEmergencyServiceDelay > 0.0;
    }

    public OptionalDouble getOptionalEmergencyGrantDelay() {
        return hasEmergencyGrantDelay() ? OptionalDouble.of(avgEmergencyGrantDelay) : OptionalDouble.empty();
    }

    public OptionalDouble getOptionalEmergencyServiceDelay() {
        return hasEmergencyServiceDelay() ? OptionalDouble.of(avgEmergencyServiceDelay) : OptionalDouble.empty();
    }

    public double getEmergencyResponseTimeToService() {
        return emergencyResponseTimeToService;
    }

    public double getReportedInternalAvgWaitingTime() {
        return reportedInternalAvgWaitingTime;
    }

    public double getReportedInternalMaxWaitingTime() {
        return reportedInternalMaxWaitingTime;
    }

    public boolean isStarvation120sTriggered() {
        return starvation120sTriggered;
    }

    public SimulationTerminationStatus getTerminationStatus() {
        return terminationStatus;
    }

    public List<VehicleMetricRecord> getVehicleRecords() {
        return vehicleRecords;
    }
}
