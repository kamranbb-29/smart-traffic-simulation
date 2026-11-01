package com.traffic.benchmark;

import com.traffic.control.Direction;
import com.traffic.control.FixedTimeStrategy;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;
import com.traffic.simulation.engine.GreenInterval;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.engine.SimulationTerminationStatus;
import com.traffic.simulation.events.MovementType;
import com.traffic.simulation.events.TrafficLightChangeEvent;
import com.traffic.simulation.events.VehicleArrivalEvent;
import com.traffic.simulation.events.VehicleDepartureEvent;
import com.traffic.simulation.events.VehicleMovementEvent;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.EmergencyVehicle;
import com.traffic.traffic.vehicle.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationMetricsTest {

    @Test
    @DisplayName("Complete simulation vehicle accounting: introduced = completed, remaining = 0")
    void testCompleteVehicleAccounting() {
        List<VehicleArrivalDescriptor> arrivals = List.of(
                new VehicleArrivalDescriptor("C1", VehicleArrivalDescriptor.VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, 0.0),
                new VehicleArrivalDescriptor("C2", VehicleArrivalDescriptor.VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, 1.0)
        );

        BenchmarkScenario scenario = new BenchmarkScenario("CompleteTest", "All vehicles finish", 2, arrivals);
        BenchmarkResult result = TrafficBenchmarkRunner.run(scenario, new FixedTimeStrategy(), "FIXED-TIME", 1);

        assertEquals(2, result.getTotalIntroduced());
        assertEquals(2, result.getCompletedCount());
        assertEquals(0, result.getQueuedCount());
        assertEquals(0, result.getInTransitCount());
        assertEquals(0, result.getRemainingCount());
        assertEquals(0, result.getPendingArrivalCount());
        assertEquals(1.0, result.getCompletionRate(), 1e-9);
        assertTrue(result.isAccountingValid());
        assertTrue(result.isSimulationComplete());
        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, result.getTerminationStatus());
    }

    @Test
    @DisplayName("Incomplete simulation vehicle accounting with phase truncation")
    void testIncompleteVehicleAccountingWithTruncatedPhases() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            arrivals.add(new VehicleArrivalDescriptor(
                    "C" + i,
                    VehicleArrivalDescriptor.VehicleType.CAR,
                    Direction.NORTH,
                    MovementType.STRAIGHT,
                    i * 1.0
            ));
        }

        // 1 phase = 10s total duration (8s GREEN + 2s YELLOW), insufficient for 10 vehicles
        BenchmarkScenario scenario = new BenchmarkScenario("TruncatedTest", "Phase truncated", 1, arrivals);
        BenchmarkResult result = TrafficBenchmarkRunner.run(scenario, new FixedTimeStrategy(), "FIXED-TIME", 1);

        assertEquals(10, result.getTotalIntroduced());
        assertTrue(result.getCompletedCount() > 0, "Some vehicles should have completed");
        assertTrue(result.getQueuedCount() > 0, "Some vehicles should remain in queue");
        assertEquals(result.getQueuedCount() + result.getInTransitCount(), result.getRemainingCount());
        assertEquals(result.getTotalIntroduced(),
                result.getCompletedCount() + result.getQueuedCount() + result.getInTransitCount() + result.getPendingArrivalCount());
        assertTrue(result.isAccountingValid());
        assertFalse(result.isSimulationComplete());
        assertTrue(result.getCompletionRate() < 1.0);
        assertEquals(SimulationTerminationStatus.PHASE_LIMIT_EXHAUSTED, result.getTerminationStatus());
    }

    @Test
    @DisplayName("In-transit vehicle accounting balances conservation invariant")
    void testInTransitVehicleAccounting() throws Exception {
        Intersection intersection = new Intersection("INT-TEST");
        SimulationEngine engine = new SimulationEngine(intersection);
        SimulationContext context = engine.getContext();

        Road road = new Road("ROAD-NORTH", Direction.NORTH);
        Lane lane = new Lane("LANE-NORTH", MovementType.STRAIGHT);
        road.addLane(lane);
        intersection.addRoad(road);

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);
        context.addTrafficSignal(signal);
        context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 0.0);

        Vehicle v1 = new Car("V1", 0.0);
        lane.addVehicle(v1);

        // Schedule movement at t=1.0 and departure at t=3.0 (with in-transit delay)
        context.getClock().advanceTime(1.0);

        // Stop engine right after VehicleMovementEvent executes, before VehicleDepartureEvent
        engine.addEventListener((event, ctx) -> {
            if (event instanceof VehicleMovementEvent) {
                engine.stop();
            }
        });

        engine.getScheduler().scheduleEvent(new VehicleMovementEvent(
                "ROAD-NORTH", "LANE-NORTH", Direction.NORTH, MovementType.STRAIGHT, 1.0, 1
        ));

        SimulationTerminationStatus status = engine.run();
        assertEquals(SimulationTerminationStatus.MANUALLY_STOPPED, status);

        // Vehicle was removed from lane and scheduled into pending departure
        assertEquals(0, intersection.getVehicleCount(), "Lane vehicle count must be 0");
        assertEquals(1, context.getPendingDepartureCount(), "Pending departure count must be 1");
        assertEquals(0, context.getCompletedVehicleCount(), "Completed count must be 0");

        // Verify BenchmarkResult accounting with in-transit vehicle
        BenchmarkResult result = new BenchmarkResult(
                "InTransitScenario", "TEST", 1,
                1, // totalIntroduced
                0, // completed
                0, // queued
                1, // inTransit
                1, // remaining = queued + inTransit
                0, // pendingArrivals
                0.0, // completionRate
                1.0, 5.0, 0.0,
                0.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 1.0,
                0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, false,
                status,
                Collections.emptyList()
        );

        assertTrue(result.isAccountingValid(), "Accounting must balance: 1 introduced = 0 completed + 0 queued + 1 inTransit + 0 pending");
        assertFalse(result.isSimulationComplete());
        assertEquals(1, result.getInTransitCount());
        assertEquals(1, result.getRemainingCount());
    }

    @Test
    @DisplayName("Completed waiting time matches t_movement - t_arrival")
    void testCompletedWaitingTimeCalculation() {
        List<VehicleArrivalDescriptor> arrivals = List.of(
                new VehicleArrivalDescriptor("C1", VehicleArrivalDescriptor.VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, 2.0)
        );

        BenchmarkScenario scenario = new BenchmarkScenario("WaitTest", "Check completed wait", 1, arrivals);
        BenchmarkResult result = TrafficBenchmarkRunner.run(scenario, new FixedTimeStrategy(), "FIXED-TIME", 1);

        assertEquals(1, result.getCompletedCount());
        // Arrival was at 2.0, movement executed at 2.0 under initial green:
        // C1 wait time = 2.0 - 2.0 = 0.0
        assertEquals(0.0, result.getAvgCompletedWaitingTime(), 1e-6);
        assertEquals(0.0, result.getMaxCompletedWaitingTime(), 1e-6);
        assertEquals(0.0, result.getAvgUnfinishedWaitSoFar(), 1e-6);

        VehicleMetricRecord record = result.getVehicleRecords().get(0);
        assertTrue(record.isCompleted());
        assertNull(record.getUnfinishedWaitSoFar());
    }

    @Test
    @DisplayName("Unfinished wait-so-far is strictly segregated from completed waiting time")
    void testUnfinishedWaitSoFarSegregation() {
        // Vehicle 1 arrives on NORTH at 1.0 (moves during NORTH green 0..8)
        // Vehicle 2 arrives on SOUTH at 2.0 (SOUTH never gets green in 1 phase)
        List<VehicleArrivalDescriptor> arrivals = List.of(
                new VehicleArrivalDescriptor("C-N1", VehicleArrivalDescriptor.VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, 1.0),
                new VehicleArrivalDescriptor("C-S1", VehicleArrivalDescriptor.VehicleType.CAR, Direction.SOUTH, MovementType.STRAIGHT, 2.0)
        );

        BenchmarkScenario scenario = new BenchmarkScenario("SegregationTest", "Segregate completed and unfinished", 1, arrivals);
        BenchmarkResult result = TrafficBenchmarkRunner.run(scenario, new FixedTimeStrategy(), "FIXED-TIME", 1);

        assertEquals(1, result.getCompletedCount());
        assertEquals(1, result.getRemainingCount());

        // Completed vehicle C-N1: arrived at 1.0, moved at 1.0 -> wait = 0.0
        assertEquals(0.0, result.getAvgCompletedWaitingTime(), 1e-6);
        assertEquals(0.0, result.getMaxCompletedWaitingTime(), 1e-6);

        // Unfinished vehicle C-S1: arrived at 2.0, simulation ended at 10.0 -> unfinished wait = 10.0 - 2.0 = 8.0
        assertTrue(result.getAvgUnfinishedWaitSoFar() > 0.0, "Unfinished wait so far must be positive");
        assertEquals(8.0, result.getAvgUnfinishedWaitSoFar(), 1e-6);
        assertEquals(8.0, result.getMaxUnfinishedWaitSoFar(), 1e-6);

        // Verify records
        for (VehicleMetricRecord rec : result.getVehicleRecords()) {
            if (rec.isCompleted()) {
                assertNull(rec.getUnfinishedWaitSoFar(), "Completed vehicle must not have unfinished wait");
            } else {
                assertNotNull(rec.getUnfinishedWaitSoFar(), "Unfinished vehicle must have unfinished wait");
                assertEquals(8.0, rec.getUnfinishedWaitSoFar(), 1e-6);
            }
        }
    }

    @Test
    @DisplayName("Zero-duration throughput handles edge case safely without NaN or Infinity")
    void testZeroDurationThroughput() {
        BenchmarkScenario scenario = new BenchmarkScenario("ZeroDuration", "Zero duration", 1, Collections.emptyList());
        BenchmarkResult result = TrafficBenchmarkRunner.run(scenario, new com.traffic.control.DensityBasedStrategy(), "DENSITY-BASED", 1);

        assertEquals(0.0, result.getThroughput());
        assertFalse(Double.isNaN(result.getThroughput()));
        assertFalse(Double.isInfinite(result.getThroughput()));
        assertEquals(0.0, result.getTotalSimulatedTime());
    }

    @Test
    @DisplayName("Emergency arrival before green light calculates grant delay = t_green - t_arr")
    void testEmergencyArrivalBeforeGreen() {
        Intersection intersection = new Intersection("INT-EMG");
        SimulationEngine engine = new SimulationEngine(intersection);
        SimulationContext context = engine.getContext();

        context.addTrafficSignal(new TrafficSignal(Direction.NORTH));

        // Signal turns GREEN at t=10.0
        context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 10.0);

        // Emergency vehicle arrived at t=4.0
        OptionalDouble grantDelay = context.getEmergencyGrantDelay(Direction.NORTH, 4.0);

        assertTrue(grantDelay.isPresent());
        assertEquals(6.0, grantDelay.getAsDouble(), 1e-6);
    }

    @Test
    @DisplayName("Emergency arrival during active green yields grant delay = 0.0")
    void testEmergencyArrivalDuringActiveGreen() {
        Intersection intersection = new Intersection("INT-EMG");
        SimulationEngine engine = new SimulationEngine(intersection);
        SimulationContext context = engine.getContext();

        context.addTrafficSignal(new TrafficSignal(Direction.NORTH));

        // Signal turned GREEN at t=0.0
        context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 0.0);

        // Emergency vehicle arrived at t=3.0 while still GREEN
        OptionalDouble grantDelay = context.getEmergencyGrantDelay(Direction.NORTH, 3.0);

        assertTrue(grantDelay.isPresent());
        assertEquals(0.0, grantDelay.getAsDouble(), 1e-6);
    }

    @Test
    @DisplayName("Emergency arrival after previous green uses next green and ignores past green")
    void testEmergencyArrivalAfterPreviousGreenInterval() {
        Intersection intersection = new Intersection("INT-EMG");
        SimulationEngine engine = new SimulationEngine(intersection);
        SimulationContext context = engine.getContext();

        context.addTrafficSignal(new TrafficSignal(Direction.NORTH));

        // First green: [0.0, 8.0]
        context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 0.0);
        context.recordSignalStateChange(Direction.NORTH, SignalState.YELLOW, 8.0);
        context.recordSignalStateChange(Direction.NORTH, SignalState.RED, 10.0);

        // Second green: [20.0, 28.0]
        context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 20.0);

        // Emergency arrives at t=12.0 (between the two greens)
        OptionalDouble grantDelay = context.getEmergencyGrantDelay(Direction.NORTH, 12.0);

        assertTrue(grantDelay.isPresent());
        // Must use second green (starts at 20.0), ignoring past green (ended at 8.0)
        assertEquals(8.0, grantDelay.getAsDouble(), 1e-6);
    }

    @Test
    @DisplayName("Emergency vehicle never receiving green represents delay as empty/censored")
    void testEmergencyVehicleNeverReceivingGreen() {
        Intersection intersection = new Intersection("INT-EMG");
        SimulationEngine engine = new SimulationEngine(intersection);
        SimulationContext context = engine.getContext();

        context.addTrafficSignal(new TrafficSignal(Direction.EAST));

        // NORTH had green, EAST never had green
        context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 0.0);

        // Emergency arrives on EAST at t=2.0
        OptionalDouble grantDelay = context.getEmergencyGrantDelay(Direction.EAST, 2.0);

        assertTrue(grantDelay.isEmpty(), "Must be empty when no green interval qualifies");
    }

    @Test
    @DisplayName("GreenInterval activeAt, startsAfter, and endedBefore semantics")
    void testGreenIntervalSemantics() {
        GreenInterval activeInterval = new GreenInterval(10.0);
        assertTrue(activeInterval.isOpen());
        assertTrue(activeInterval.isActiveAt(10.0), "Start boundary of open interval must be active");
        assertTrue(activeInterval.isActiveAt(15.0), "Open interval remains active after its start");
        assertFalse(activeInterval.isActiveAt(5.0), "Before start time must be inactive");
        assertTrue(activeInterval.startsAfter(5.0), "startsAfter() is true for strictly earlier timestamp");
        assertFalse(activeInterval.startsAfter(10.0), "startsAfter() is false for equal timestamp");
        assertFalse(activeInterval.startsAfter(12.0), "startsAfter() is false for later timestamp");
        assertFalse(activeInterval.endedBefore(12.0));

        GreenInterval closedInterval = new GreenInterval(10.0);
        closedInterval.close(20.0);
        assertFalse(closedInterval.isOpen());
        assertTrue(closedInterval.isActiveAt(10.0), "The start boundary is active");
        assertTrue(closedInterval.isActiveAt(15.0));
        assertTrue(closedInterval.isActiveAt(19.99), "A timestamp just before the end is active");
        assertFalse(closedInterval.isActiveAt(20.0), "The exact end boundary is inactive");
        assertFalse(closedInterval.isActiveAt(25.0), "A timestamp after the end is inactive");
        assertTrue(closedInterval.endedBefore(25.0));
        assertFalse(closedInterval.endedBefore(15.0));

        assertFalse(closedInterval.startsAfter(10.0), "startsAfter() returns false for equal timestamp");
        assertTrue(closedInterval.startsAfter(9.99), "startsAfter() returns true for strictly later timestamp");

        assertThrows(IllegalArgumentException.class, () -> new GreenInterval(-1.0));
        assertThrows(IllegalArgumentException.class, () -> {
            GreenInterval gi = new GreenInterval(20.0);
            gi.close(10.0);
        });
    }

    @Test
    @DisplayName("GreenInterval half-open boundary table verification: [0, 8) and [10, infinity)")
    void testGreenIntervalHalfOpenBoundaryTable() {
        // Closed interval [0, 8)
        GreenInterval closed = new GreenInterval(0.0);
        closed.close(8.0);

        assertTrue(closed.isActiveAt(0.0), "[0, 8) at 0 -> Yes");
        assertTrue(closed.isActiveAt(7.99), "[0, 8) at 7.99 -> Yes");
        assertFalse(closed.isActiveAt(8.0), "[0, 8) at 8 -> No");
        assertFalse(closed.isActiveAt(9.0), "[0, 8) at 9 -> No");

        // Open interval [10, infinity)
        GreenInterval open = new GreenInterval(10.0);
        assertTrue(open.isActiveAt(15.0), "[10, infinity) at 15 -> Yes");
        assertTrue(open.isActiveAt(10.0), "[10, infinity) at 10 -> Yes");
        assertFalse(open.isActiveAt(9.99), "[10, infinity) at 9.99 -> No");
    }

    @Test
    @DisplayName("Emergency arrival at exact YELLOW transition boundary waits for next GREEN")
    void testEmergencyArrivalAtExactYellowTransitionBoundary() {
        Intersection intersection = new Intersection("INT-EMG-BOUND");
        SimulationEngine engine = new SimulationEngine(intersection);
        SimulationContext context = engine.getContext();

        context.addTrafficSignal(new TrafficSignal(Direction.NORTH));

        // 1. A GREEN interval begins at t=0.
        context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 0.0);

        // 2. It closes when the signal changes to YELLOW at t=8.
        context.recordSignalStateChange(Direction.NORTH, SignalState.YELLOW, 8.0);

        // 3. The next GREEN interval starts at t=20.
        context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 20.0);

        // 4. An emergency vehicle arrives at t=8.
        OptionalDouble grantDelay = context.getEmergencyGrantDelay(Direction.NORTH, 8.0);

        // 5. Expected grant delay: 12 seconds.
        assertTrue(grantDelay.isPresent(), "Grant delay must be present");
        assertEquals(12.0, grantDelay.getAsDouble(), 1e-6, "Grant delay must be 20 - 8 = 12 seconds");
    }

    @Test
    @DisplayName("Termination status correctly reflects execution outcome")
    void testTerminationStatusPropagation() {
        // Normal completion
        List<VehicleArrivalDescriptor> arrivals = List.of(
                new VehicleArrivalDescriptor("C1", VehicleArrivalDescriptor.VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, 0.0)
        );
        BenchmarkScenario s1 = new BenchmarkScenario("S1", "Normal", 1, arrivals);
        BenchmarkResult r1 = TrafficBenchmarkRunner.run(s1, new FixedTimeStrategy(), "FIXED-TIME", 1);
        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, r1.getTerminationStatus());

        // Phase limit exhausted
        List<VehicleArrivalDescriptor> heavyArrivals = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            heavyArrivals.add(new VehicleArrivalDescriptor("H" + i, VehicleArrivalDescriptor.VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, i * 1.0));
        }
        BenchmarkScenario s2 = new BenchmarkScenario("S2", "Exhausted", 1, heavyArrivals);
        BenchmarkResult r2 = TrafficBenchmarkRunner.run(s2, new FixedTimeStrategy(), "FIXED-TIME", 1);
        assertEquals(SimulationTerminationStatus.PHASE_LIMIT_EXHAUSTED, r2.getTerminationStatus());
    }

    @Test
    @DisplayName("Empty workload metrics behave safely and accurately")
    void testEmptyWorkloadMetrics() {
        BenchmarkScenario emptyScenario = new BenchmarkScenario("Empty", "No arrivals", 5, Collections.emptyList());
        BenchmarkResult result = TrafficBenchmarkRunner.run(emptyScenario, new FixedTimeStrategy(), "FIXED-TIME", 1);

        assertEquals(0, result.getTotalIntroduced());
        assertEquals(0, result.getCompletedCount());
        assertEquals(0, result.getQueuedCount());
        assertEquals(0, result.getInTransitCount());
        assertEquals(0, result.getRemainingCount());
        assertEquals(0, result.getPendingArrivalCount());
        assertEquals(1.0, result.getCompletionRate(), 1e-9);
        assertEquals(0.0, result.getThroughput());
        assertTrue(result.isAccountingValid());
        assertTrue(result.isSimulationComplete());
        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, result.getTerminationStatus());
    }

    @Test
    @DisplayName("Accounting invariant violations are detected by isAccountingValid()")
    void testAccountingInvariantViolationDetected() {
        BenchmarkResult invalidResult = new BenchmarkResult(
                "InvalidScenario", "TEST", 1,
                10, // totalIntroduced
                5,  // completed
                2,  // queued
                1,  // inTransit
                3,  // remaining = 2 + 1
                0,  // pendingArrivals -> 5 + 2 + 1 + 0 = 8 != 10
                0.5,
                10.0, 5.0, 0.5,
                2.0, 4.0, 1.0,
                1.5, 3.0, 2.5, 3.5,
                0.0, 0.0, 0.0, 0.0, 0.0,
                1.5, 3.0, false,
                SimulationTerminationStatus.PHASE_LIMIT_EXHAUSTED,
                Collections.emptyList()
        );

        assertFalse(invalidResult.isAccountingValid(), "8 accounted != 10 introduced must be invalid");
    }

    @Test
    @DisplayName("Emergency vehicle arriving and served at t=0 has service delay 0.0 without fallback")
    void testEmergencyVehicleZeroServiceDelayNoFallback() {
        List<VehicleArrivalDescriptor> arrivals = List.of(
                new VehicleArrivalDescriptor("EMG-1", VehicleArrivalDescriptor.VehicleType.EMERGENCY, Direction.NORTH, MovementType.STRAIGHT, 0.0)
        );

        BenchmarkScenario scenario = new BenchmarkScenario("EmgZeroDelay", "Zero service delay", 1, arrivals);
        BenchmarkResult result = TrafficBenchmarkRunner.run(scenario, new FixedTimeStrategy(), "FIXED-TIME", 1);

        assertEquals(1, result.getCompletedCount());
        assertEquals(0.0, result.getAvgEmergencyServiceDelay(), 1e-6);
        assertEquals(0.0, result.getEmergencyResponseTimeToService(), 1e-6,
                "Reported response time to service must remain 0.0 and not fall back or be treated as missing");

        VehicleMetricRecord record = result.getVehicleRecords().get(0);
        assertTrue(record.isEmergency());
        assertEquals(0.0, record.getEmergencyServiceDelay(), 1e-6);
    }

    @Test
    @DisplayName("Emergency vehicle with no observed service delay falls back to grant delay")
    void testEmergencyVehicleNoObservedServiceDelayFallback() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();
        // 10 cars ahead of the emergency vehicle on EAST arriving at t=0.0
        for (int i = 1; i <= 10; i++) {
            arrivals.add(new VehicleArrivalDescriptor(
                    "C-E" + i, VehicleArrivalDescriptor.VehicleType.CAR, Direction.EAST, MovementType.STRAIGHT, 0.0
            ));
        }
        // Emergency vehicle is queued behind the 10 cars on EAST arriving at t=1.0
        arrivals.add(new VehicleArrivalDescriptor(
                "EMG-E1", VehicleArrivalDescriptor.VehicleType.EMERGENCY, Direction.EAST, MovementType.STRAIGHT, 1.0
        ));

        // 2 phases: Phase 1 NORTH (0..10), Phase 2 EAST (10..20)
        // During Phase 2, EAST green is 10..18 (8s). Headway is 1.0s, so at most 8 cars pass.
        // EMG-E1 is 11th in line, so it never executes movement before phase limit terminates at t=20.
        BenchmarkScenario scenario = new BenchmarkScenario("EmgStuckInQueue", "No service delay observed", 2, arrivals);
        BenchmarkResult result = TrafficBenchmarkRunner.run(scenario, new FixedTimeStrategy(), "FIXED-TIME", 1);

        // Emergency vehicle never moved
        assertEquals(0.0, result.getAvgEmergencyServiceDelay(), 1e-6, "No service delay should be recorded");

        // But EAST received GREEN starting at t=10.0, so grant delay is 10.0 - 1.0 = 9.0s
        assertEquals(9.0, result.getAvgEmergencyGrantDelay(), 1e-6, "Grant delay must be 10 - 1 = 9s");

        // emergencyResponseTimeToService must fall back to avgEmergencyGrantDelay (9.0s)
        assertEquals(9.0, result.getEmergencyResponseTimeToService(), 1e-6,
                "Must fall back to grant delay when no service delay was observed");
    }

    @Test
    @DisplayName("Multiple emergency vehicles with mixture of zero and positive service delays include 0.0 in average")
    void testMultipleEmergencyVehiclesMixedZeroAndPositiveServiceDelays() {
        List<VehicleArrivalDescriptor> arrivals = List.of(
                new VehicleArrivalDescriptor("EMG-A", VehicleArrivalDescriptor.VehicleType.EMERGENCY, Direction.NORTH, MovementType.STRAIGHT, 0.0),
                new VehicleArrivalDescriptor("EMG-B", VehicleArrivalDescriptor.VehicleType.EMERGENCY, Direction.NORTH, MovementType.STRAIGHT, 0.0)
        );

        // Phase 1 NORTH green 0..8s.
        // EMG-A moves at 0.0 -> service delay = 0.0 - 0.0 = 0.0.
        // EMG-B moves at 1.0 -> service delay = 1.0 - 0.0 = 1.0.
        // Average service delay = (0.0 + 1.0) / 2 = 0.5s.
        BenchmarkScenario scenario = new BenchmarkScenario("EmgMixedDelays", "Mixed service delays", 1, arrivals);
        BenchmarkResult result = TrafficBenchmarkRunner.run(scenario, new FixedTimeStrategy(), "FIXED-TIME", 1);

        assertEquals(2, result.getCompletedCount());
        assertEquals(0.5, result.getAvgEmergencyServiceDelay(), 1e-6, "Average must include valid zero-valued observation");
        assertEquals(0.5, result.getEmergencyResponseTimeToService(), 1e-6,
                "Reported response time to service must match average service delay");
    }
}
