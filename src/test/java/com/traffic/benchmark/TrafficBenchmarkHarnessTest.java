package com.traffic.benchmark;

import com.traffic.control.AdaptivePriorityStrategy;
import com.traffic.control.DensityBasedStrategy;
import com.traffic.control.Direction;
import com.traffic.control.FixedTimeStrategy;
import com.traffic.simulation.events.MovementType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficBenchmarkHarnessTest {

    @Test
    void testVehicleAccountingOnCompleteSimulation() {
        BenchmarkScenario scenario = ScenarioFactory.createLightTrafficScenario();
        BenchmarkResult result = TrafficBenchmarkRunner.run(
                scenario,
                new FixedTimeStrategy(),
                "FIXED-TIME",
                1
        );

        assertTrue(result.isAccountingValid(), "Vehicle accounting introduced = completed + remaining must hold");
        assertTrue(result.isSimulationComplete(), "Light traffic should complete all vehicles within 8 phases");
        assertEquals(scenario.getVehicleCount(), result.getTotalIntroduced());
        assertEquals(result.getTotalIntroduced(), result.getCompletedCount());
        assertEquals(0, result.getRemainingCount());
        assertTrue(result.getThroughput() > 0.0);
    }

    @Test
    void testIncompleteSimulationDetectedAndAccounted() {
        // Intentionally create a heavy scenario with only 1 phase (10s max duration)
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            arrivals.add(new VehicleArrivalDescriptor(
                    "INCOMP-" + i,
                    VehicleArrivalDescriptor.VehicleType.CAR,
                    Direction.NORTH,
                    MovementType.STRAIGHT,
                    i * 2.0
            ));
        }

        BenchmarkScenario starvedScenario = new BenchmarkScenario(
                "Truncated Scenario",
                "Scenario truncated to 1 phase to test harness incomplete simulation detection",
                1,
                arrivals
        );

        BenchmarkResult result = TrafficBenchmarkRunner.run(
                starvedScenario,
                new FixedTimeStrategy(),
                "FIXED-TIME",
                1
        );

        assertTrue(result.isAccountingValid(), "Accounting must balance even when truncated");
        assertFalse(result.isSimulationComplete(), "Simulation must be flagged as INCOMPLETE");
        assertTrue(result.getRemainingCount() > 0, "Vehicles should remain in queue");
        assertEquals(20, result.getTotalIntroduced());
        assertEquals(result.getTotalIntroduced(), result.getCompletedCount() + result.getRemainingCount());
    }

    @Test
    void testInternalWaitingTimeTracking() {
        BenchmarkScenario scenario = ScenarioFactory.createLightTrafficScenario();
        BenchmarkResult result = TrafficBenchmarkRunner.run(
                scenario,
                new DensityBasedStrategy(),
                "DENSITY-BASED",
                1
        );

        // Verify that internal waiting-time metric is now properly tracked (> 0.0)
        assertTrue(result.getReportedInternalAvgWaitingTime() > 0.0, "Internal average waiting time should be positive");
        assertTrue(result.getReportedInternalMaxWaitingTime() >= result.getReportedInternalAvgWaitingTime(),
                "Internal max waiting time must be at least average waiting time");

        // But external time in system must be properly measured and positive
        assertTrue(result.getAvgTimeInSystem() > 0.0, "Time in system (departure - arrival) must be positive");
    }

    @Test
    void testTimeInSystemCalculation() {
        BenchmarkScenario scenario = ScenarioFactory.createBalancedTrafficScenario();
        BenchmarkResult result = TrafficBenchmarkRunner.run(
                scenario,
                new AdaptivePriorityStrategy(),
                "ADAPTIVE-PRIORITY",
                1
        );

        assertTrue(result.getAvgTimeInSystem() > 0.0);
        assertTrue(result.getMaxTimeInSystem() >= result.getAvgTimeInSystem());
        assertTrue(result.getMinTimeInSystem() <= result.getAvgTimeInSystem());
        assertTrue(result.getMinTimeInSystem() >= 0.0);
    }

    @Test
    void testEmergencyResponseTimeMeasurement() {
        BenchmarkScenario scenario = ScenarioFactory.createEmergencyTrafficScenario();
        BenchmarkResult result = TrafficBenchmarkRunner.run(
                scenario,
                new AdaptivePriorityStrategy(),
                "ADAPTIVE-PRIORITY",
                1
        );

        assertTrue(result.getAvgEmergencyTimeInSystem() > 0.0, "Emergency time in system must be tracked");
        assertTrue(result.getEmergencyResponseTimeToService() >= 0.0, "Emergency delay to first green must be non-negative");
    }

    @Test
    void testStarvationRuleNeverTriggeredDueToZeroWait() {
        BenchmarkScenario scenario = ScenarioFactory.createStarvationScenario();
        BenchmarkResult result = TrafficBenchmarkRunner.run(
                scenario,
                new AdaptivePriorityStrategy(),
                "ADAPTIVE-PRIORITY",
                1
        );

        assertFalse(result.isStarvation120sTriggered(),
                "Adaptive 120s starvation threshold cannot be triggered due to unupdated waiting times");
    }

    @Test
    void testIdenticalScenarioReplayAcrossStrategies() {
        BenchmarkScenario scenario = ScenarioFactory.createLightTrafficScenario();

        BenchmarkResult r1 = TrafficBenchmarkRunner.run(scenario, new FixedTimeStrategy(), "FIXED-TIME", 1);
        BenchmarkResult r2 = TrafficBenchmarkRunner.run(scenario, new DensityBasedStrategy(), "DENSITY-BASED", 1);
        BenchmarkResult r3 = TrafficBenchmarkRunner.run(scenario, new AdaptivePriorityStrategy(), "ADAPTIVE-PRIORITY", 1);

        assertEquals(r1.getTotalIntroduced(), r2.getTotalIntroduced());
        assertEquals(r1.getTotalIntroduced(), r3.getTotalIntroduced());
    }
}

