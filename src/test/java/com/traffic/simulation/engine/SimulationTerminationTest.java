package com.traffic.simulation.engine;

import com.traffic.control.*;
import com.traffic.simulation.events.*;
import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.EmergencyVehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationTerminationTest {

    private static class Fixture {
        SimulationEngine engine;
        SimulationContext context;
        Intersection intersection;
        EventScheduler scheduler;
        Road northRoad;
        Lane northLane;
        Road southRoad;
        Lane southLane;
        Road eastRoad;
        Lane eastLane;
        Road westRoad;
        Lane westLane;
    }

    private Fixture f;

    @BeforeEach
    void setUp() {
        f = new Fixture();
        f.intersection = new Intersection("INT-TERM");

        for (Direction dir : Direction.values()) {
            Road road = new Road("ROAD-" + dir, dir);
            Lane lane = new Lane("LANE-" + dir, MovementType.STRAIGHT);
            road.addLane(lane);
            f.intersection.addRoad(road);
        }

        f.northRoad = f.intersection.getRoads().stream().filter(r -> r.getDirection() == Direction.NORTH).findFirst().orElseThrow();
        f.northLane = f.northRoad.getLanes().get(0);
        f.southRoad = f.intersection.getRoads().stream().filter(r -> r.getDirection() == Direction.SOUTH).findFirst().orElseThrow();
        f.southLane = f.southRoad.getLanes().get(0);
        f.eastRoad = f.intersection.getRoads().stream().filter(r -> r.getDirection() == Direction.EAST).findFirst().orElseThrow();
        f.eastLane = f.eastRoad.getLanes().get(0);
        f.westRoad = f.intersection.getRoads().stream().filter(r -> r.getDirection() == Direction.WEST).findFirst().orElseThrow();
        f.westLane = f.westRoad.getLanes().get(0);

        f.engine = new SimulationEngine(f.intersection);
        f.context = f.engine.getContext();
        f.scheduler = f.engine.getScheduler();

        for (Direction dir : Direction.values()) {
            f.context.addTrafficSignal(new TrafficSignal(dir));
        }
    }

    @Test
    @DisplayName("1. All vehicles complete -> NORMAL_COMPLETION")
    void testAllVehiclesCompleteYieldsNormalCompletion() {
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C1", 0.0), "ROAD-NORTH", 0.0, 1));
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C2", 1.0), "ROAD-NORTH", 1.0, 1));
        f.scheduler.scheduleEvent(new StrategyControllerEvent(new DensityBasedStrategy(), 4, 0.0, 2));

        SimulationTerminationStatus status = f.engine.run();

        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, status);
        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, f.engine.getTerminationStatus());
        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, f.context.getTerminationStatus());
        assertEquals(2, f.context.getCompletedVehicleCount());
        assertEquals(0, f.intersection.getVehicleCount());
    }

    @Test
    @DisplayName("2. No vehicles remain and no future arrivals exist -> NORMAL_COMPLETION")
    void testEmptyWorkloadYieldsNormalCompletion() {
        SimulationTerminationStatus status = f.engine.run();

        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, status);
        assertEquals(0, f.context.getCompletedVehicleCount());
        assertEquals(0, f.intersection.getVehicleCount());
    }

    @Test
    @DisplayName("3. Phase budget expires while vehicles remain -> PHASE_LIMIT_EXHAUSTED")
    void testPhaseBudgetExhaustedWithRemainingVehicles() {
        // 1 vehicle in NORTH, 2 vehicles in SOUTH
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C-NORTH", 0.0), "ROAD-NORTH", 0.0, 1));
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C-SOUTH-1", 0.0), "ROAD-SOUTH", 0.0, 1));
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C-SOUTH-2", 1.0), "ROAD-SOUTH", 1.0, 1));

        // FixedTimeStrategy with exactly 1 phase will only serve NORTH (first in cycle)
        f.scheduler.scheduleEvent(new StrategyControllerEvent(new FixedTimeStrategy(), 1, 0.0, 2));

        SimulationTerminationStatus status = f.engine.run();

        assertEquals(SimulationTerminationStatus.PHASE_LIMIT_EXHAUSTED, status);
        assertEquals(SimulationTerminationStatus.PHASE_LIMIT_EXHAUSTED, f.engine.getTerminationStatus());
        assertTrue(f.context.isPhaseBudgetExhausted());
        assertEquals(1, f.context.getCompletedVehicleCount(), "Only vehicle in NORTH should have completed");
        assertEquals(2, f.intersection.getVehicleCount(), "Vehicles in SOUTH should remain");
    }

    @Test
    @DisplayName("4. Vehicles remain blocked with no useful future events -> STALLED_DEADLOCK")
    void testBlockedVehiclesWithoutRecoveryYieldsStalledDeadlock() {
        // Vehicle in NORTH lane, and lane is blocked by accident
        f.northLane.addVehicle(new Car("C-BLOCKED", 0.0));
        f.northLane.blockLane();

        // No recovery event and no pending arrivals
        SimulationTerminationStatus status = f.engine.run();

        assertEquals(SimulationTerminationStatus.STALLED_DEADLOCK, status);
        assertEquals(SimulationTerminationStatus.STALLED_DEADLOCK, f.engine.getTerminationStatus());
        assertEquals(1, f.intersection.getVehicleCount());
        assertEquals(0, f.context.getCompletedVehicleCount());
    }

    @Test
    @DisplayName("5. Vehicles remain blocked but a future recovery event exists -> simulation continues")
    void testBlockedVehiclesWithFutureRecoveryContinuesSimulation() {
        f.northLane.addVehicle(new Car("C1", 0.0));
        f.northLane.blockLane();

        // Recovery event scheduled at t=20.0
        f.scheduler.scheduleEvent(new LaneUnblockEvent("ROAD-NORTH", "LANE-NORTH", 20.0, 1));

        // Controller with enough phases
        f.scheduler.scheduleEvent(new StrategyControllerEvent(new DensityBasedStrategy(), 4, 0.0, 2));

        SimulationTerminationStatus status = f.engine.run();

        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, status);
        assertTrue(f.engine.getClock().getCurrentTime() >= 20.0, "Clock should have reached unblock time");
        assertEquals(1, f.context.getCompletedVehicleCount());
        assertEquals(0, f.intersection.getVehicleCount());
    }

    @Test
    @DisplayName("6. Future normal arrival exists -> simulation does not terminate prematurely")
    void testFutureNormalArrivalPreventsPrematureDeadlock() {
        // Vehicle in NORTH is blocked
        f.northLane.addVehicle(new Car("C-STUCK", 0.0));
        f.northLane.blockLane();

        // Future unblocked arrival on EAST at t=25.0 (2 cars so EAST has higher density than NORTH)
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C-EAST-1", 25.0), "ROAD-EAST", 25.0, 1));
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C-EAST-2", 25.0), "ROAD-EAST", 25.0, 1));

        // Controller scheduled
        f.scheduler.scheduleEvent(new StrategyControllerEvent(new DensityBasedStrategy(), 5, 0.0, 2));

        // Engine run
        f.engine.run();

        // The vehicle on EAST should have arrived at 25.0 and completed
        assertTrue(f.context.getCompletedVehicleCount() >= 1, "EAST vehicle must complete");
        assertTrue(f.engine.getClock().getCurrentTime() >= 25.0);
    }

    @Test
    @DisplayName("7. Future emergency arrival exists -> simulation does not terminate prematurely")
    void testFutureEmergencyArrivalPreventsPrematureDeadlock() {
        // Vehicle in NORTH is blocked
        f.northLane.addVehicle(new Car("C-STUCK", 0.0));
        f.northLane.blockLane();

        // Future emergency arrival on EAST at t=25.0
        f.scheduler.scheduleEvent(new EmergencyArrivalEvent(new EmergencyVehicle("AMB-1", 25.0), "LANE-EAST", 25.0, 1));

        // Controller scheduled with AdaptivePriorityStrategy (prioritizes emergency vehicles)
        f.scheduler.scheduleEvent(new StrategyControllerEvent(new AdaptivePriorityStrategy(), 5, 0.0, 2));

        // Engine run
        f.engine.run();

        // Emergency vehicle must be served
        assertEquals(1, f.context.getCompletedVehicleCount(), "AMB-1 must complete");
        assertTrue(f.engine.getClock().getCurrentTime() >= 25.0);
    }

    @Test
    @DisplayName("8. Recurring controller events alone cannot keep a deadlocked simulation alive indefinitely")
    void testRecurringControllerEventsDoNotPreventDeadlockTermination() {
        // Vehicle in NORTH is blocked
        f.northLane.addVehicle(new Car("C-DEADLOCK", 0.0));
        f.northLane.blockLane();

        // Controller with 100 phases
        f.scheduler.scheduleEvent(new StrategyControllerEvent(new DensityBasedStrategy(), 100, 0.0, 2));

        SimulationTerminationStatus status = f.engine.run();

        assertEquals(SimulationTerminationStatus.STALLED_DEADLOCK, status);
        // It should have halted immediately without executing all 100 phases
        assertTrue(f.engine.getProcessedEventsCount() < 5,
                "Stall detector must stop simulation without cycling through all controller phases");
    }

    @Test
    @DisplayName("9. Event-processing limit reached -> LOOP_LIMIT_EXHAUSTED")
    void testLoopLimitExhaustion() {
        // Set a small safety limit of 10 events
        f.engine.setMaxEventLimit(10);
        assertEquals(10, f.engine.getMaxEventLimit());

        // Schedule 20 harmless events
        for (int i = 0; i < 20; i++) {
            final int idx = i;
            f.scheduler.scheduleEvent(new SimulationEvent(i, "Dummy_" + i, 1) {
                @Override
                public void executeEvent(SimulationContext context) {
                    // no-op
                }
            });
        }

        SimulationTerminationStatus status = f.engine.run();

        assertEquals(SimulationTerminationStatus.LOOP_LIMIT_EXHAUSTED, status);
        assertEquals(SimulationTerminationStatus.LOOP_LIMIT_EXHAUSTED, f.engine.getTerminationStatus());
        assertEquals(10, f.engine.getProcessedEventsCount());
    }

    @Test
    @DisplayName("10. Successful movements update progress tracking correctly")
    void testSuccessfulMovementsUpdateProgressTracking() {
        assertEquals(0, f.context.getSuccessfulMovementCount());
        assertTrue(f.context.getLastSuccessfulMovementTime().isEmpty());

        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C1", 0.0), "ROAD-NORTH", 0.0, 1));
        f.scheduler.scheduleEvent(new StrategyControllerEvent(new DensityBasedStrategy(), 2, 0.0, 2));

        f.engine.run();

        assertEquals(1, f.context.getSuccessfulMovementCount());
        OptionalDouble lastTime = f.context.getLastSuccessfulMovementTime();
        assertTrue(lastTime.isPresent());
        assertEquals(0.0, lastTime.getAsDouble(), 1e-6);
    }

    @Test
    @DisplayName("11. Failed/paused movements do not increment successful movement count")
    void testFailedMovementsDoNotIncrementProgressCount() {
        // Add car to lane
        Car car = new Car("C1", 0.0);
        f.northLane.addVehicle(car);

        // Signal is RED, road is blocked
        f.northRoad.blockRoad();

        // Attempt movement event directly
        VehicleMovementEvent movementEvent = new VehicleMovementEvent(
                "ROAD-NORTH", "LANE-NORTH", Direction.NORTH, MovementType.STRAIGHT, 5.0, 1
        );
        movementEvent.executeEvent(f.context);

        // Movement paused: successful count should remain 0
        assertEquals(0, f.context.getSuccessfulMovementCount());
        assertTrue(f.context.getLastSuccessfulMovementTime().isEmpty());
    }

    @Test
    @DisplayName("12. Manual termination is reported correctly via engine.stop()")
    void testManualStopIsReportedCorrectly() {
        f.scheduler.scheduleEvent(new SimulationEvent(10.0, "Stopping_Event", 1) {
            @Override
            public void executeEvent(SimulationContext context) {
                f.engine.stop();
            }
        });
        f.scheduler.scheduleEvent(new SimulationEvent(20.0, "After_Stop_Event", 1) {
            @Override
            public void executeEvent(SimulationContext context) {
                fail("This event should never execute after stop");
            }
        });

        SimulationTerminationStatus status = f.engine.run();

        assertEquals(SimulationTerminationStatus.MANUALLY_STOPPED, status);
        assertEquals(SimulationTerminationStatus.MANUALLY_STOPPED, f.engine.getTerminationStatus());
        assertEquals(1, f.engine.getProcessedEventsCount());
        assertFalse(f.engine.isRunning());
    }

    @Test
    @DisplayName("Edge Case: Reset clears progress and resets status")
    void testResetClearsProgressTracking() {
        f.context.recordSuccessfulMovement(15.0);
        assertEquals(1, f.context.getSuccessfulMovementCount());
        assertTrue(f.context.getLastSuccessfulMovementTime().isPresent());

        f.engine.reset();

        assertEquals(0, f.context.getSuccessfulMovementCount());
        assertTrue(f.context.getLastSuccessfulMovementTime().isEmpty());
        assertNull(f.engine.getTerminationStatus());
        assertFalse(f.context.isPhaseBudgetExhausted());
    }

    @Test
    @DisplayName("13. Active GREEN interval is closed at termination (MANUALLY_STOPPED path)")
    void testActiveGreenIntervalClosedOnManualStop() {
        // Pre-existing closed interval on SOUTH: [0.0, 5.0)
        f.context.recordSignalStateChange(Direction.SOUTH, SignalState.GREEN, 0.0);
        f.context.recordSignalStateChange(Direction.SOUTH, SignalState.YELLOW, 5.0);

        // Open interval on NORTH: [10.0, infinity)
        f.context.recordSignalStateChange(Direction.NORTH, SignalState.GREEN, 10.0);

        // Advance simulation and stop at t=15.0
        f.scheduler.scheduleEvent(new SimulationEvent(15.0, "Stop_Event", 1) {
            @Override
            public void executeEvent(SimulationContext context) {
                f.engine.stop();
            }
        });

        SimulationTerminationStatus status = f.engine.run();
        assertEquals(SimulationTerminationStatus.MANUALLY_STOPPED, status);

        // Verify NORTH interval was finalized at termination clock time (15.0)
        GreenInterval northInterval = f.context.getGreenIntervals(Direction.NORTH).get(0);
        assertFalse(northInterval.isOpen(), "Interval must be closed");
        assertEquals(15.0, northInterval.getEndTime(), 1e-6, "End time must be exactly 15.0, not infinity");
        assertTrue(northInterval.isActiveAt(14.99), "isActiveAt(14.99) must be true");
        assertFalse(northInterval.isActiveAt(15.0), "isActiveAt(15.0) must be false under half-open semantics");

        // Verify already-closed SOUTH interval was untouched
        GreenInterval southInterval = f.context.getGreenIntervals(Direction.SOUTH).get(0);
        assertFalse(southInterval.isOpen());
        assertEquals(5.0, southInterval.getEndTime(), 1e-6, "Already-closed interval end time must not change");
        assertTrue(southInterval.isActiveAt(4.99));
        assertFalse(southInterval.isActiveAt(5.0));

        // Verify repeated finalization is safe and idempotent
        f.context.closeActiveGreenIntervals(20.0);
        assertEquals(15.0, northInterval.getEndTime(), 1e-6, "Repeated finalization must not alter closed interval");
        assertEquals(5.0, southInterval.getEndTime(), 1e-6);
    }

    @Test
    @DisplayName("14. Active GREEN interval is closed at termination (LOOP_LIMIT_EXHAUSTED path)")
    void testActiveGreenIntervalClosedOnLoopLimitExhaustion() {
        // Set safety limit to 5 events
        f.engine.setMaxEventLimit(5);

        // Open interval on EAST: [10.0, infinity)
        f.context.recordSignalStateChange(Direction.EAST, SignalState.GREEN, 10.0);

        // Schedule events so the 5th event executes at t=15.0
        for (int i = 1; i <= 10; i++) {
            f.scheduler.scheduleEvent(new SimulationEvent(10.0 + i, "Event_" + i, 1) {
                @Override
                public void executeEvent(SimulationContext context) {}
            });
        }

        SimulationTerminationStatus status = f.engine.run();
        assertEquals(SimulationTerminationStatus.LOOP_LIMIT_EXHAUSTED, status);
        assertEquals(15.0, f.context.getClock().getCurrentTime(), 1e-6);

        // Verify EAST interval was finalized at t=15.0
        GreenInterval eastInterval = f.context.getGreenIntervals(Direction.EAST).get(0);
        assertFalse(eastInterval.isOpen());
        assertEquals(15.0, eastInterval.getEndTime(), 1e-6, "End time must be 15.0");
        assertTrue(eastInterval.isActiveAt(14.99));
        assertFalse(eastInterval.isActiveAt(15.0));
    }

    @Test
    @DisplayName("15. Active GREEN interval is closed at termination (NORMAL_COMPLETION path)")
    void testActiveGreenIntervalClosedOnNormalCompletion() {
        // Open interval on WEST: [10.0, infinity)
        f.context.recordSignalStateChange(Direction.WEST, SignalState.GREEN, 10.0);

        // Schedule final event at t=15.0
        f.scheduler.scheduleEvent(new SimulationEvent(15.0, "Final_Event", 1) {
            @Override
            public void executeEvent(SimulationContext context) {}
        });

        SimulationTerminationStatus status = f.engine.run();
        assertEquals(SimulationTerminationStatus.NORMAL_COMPLETION, status);
        assertEquals(15.0, f.context.getClock().getCurrentTime(), 1e-6);

        GreenInterval westInterval = f.context.getGreenIntervals(Direction.WEST).get(0);
        assertFalse(westInterval.isOpen());
        assertEquals(15.0, westInterval.getEndTime(), 1e-6, "End time must be 15.0");
        assertTrue(westInterval.isActiveAt(14.99));
        assertFalse(westInterval.isActiveAt(15.0));
    }
}
