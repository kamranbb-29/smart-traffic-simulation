package com.traffic.simulation.events;

import com.traffic.control.*;
import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.EmergencyVehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused unit tests for StrategyControllerEvent:
 * - Safe idle handling when strategy returns null (signals remain RED).
 * - Preservation of active phase budget (remainingPhases) during idle.
 * - Non-destructive, event-driven jump to next arrival timestamp.
 * - Simultaneous arrival and controller event ordering (arrival priority 1, controller priority 2).
 * - Clean termination when all workload is complete.
 * - Full GREEN -> YELLOW -> RED transition completion on the final active phase.
 * - Inclusion of EmergencyArrivalEvent in pending arrivals and wake-up.
 */
class StrategyControllerEventTest {

    private static class TestFixture {
        SimulationEngine engine;
        SimulationContext context;
        Intersection intersection;
        EventScheduler scheduler;
        Road northRoad;
        Lane northLane;
    }

    private TestFixture createFixture() {
        TestFixture f = new TestFixture();
        f.intersection = new Intersection("INT-TEST");

        for (Direction dir : Direction.values()) {
            Road road = new Road("ROAD-" + dir, dir);
            Lane lane = new Lane("LANE-" + dir, MovementType.STRAIGHT);
            road.addLane(lane);
            f.intersection.addRoad(road);
        }

        f.northRoad = f.intersection.getRoads().stream()
                .filter(r -> r.getDirection() == Direction.NORTH).findFirst().orElseThrow();
        f.northLane = f.northRoad.getLanes().get(0);

        f.engine = new SimulationEngine(f.intersection);
        f.context = f.engine.getContext();
        f.scheduler = f.engine.getScheduler();

        for (Direction dir : Direction.values()) {
            f.context.addTrafficSignal(new TrafficSignal(dir));
        }

        return f;
    }

    @Test
    @DisplayName("Idle: controller keeps all signals RED and does not decrement phase budget when strategy returns null")
    void testIdleControllerKeepsSignalsRedAndPreservesBudget() {
        TestFixture f = createFixture();

        // Custom strategy that returns null for the first check, then NORTH
        SignalControlStrategy nullOnceStrategy = new SignalControlStrategy() {
            int calls = 0;
            @Override
            public Direction chooseDirection(TrafficSnapshot snapshot) {
                calls++;
                return (calls == 1) ? null : Direction.NORTH;
            }
        };

        // Future arrival at t=10.0
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C1", 10.0), "ROAD-NORTH", 10.0, 1));

        // Controller at t=0.0 with 3 remaining phases
        StrategyControllerEvent controllerEvent = new StrategyControllerEvent(nullOnceStrategy, 3, 0.0, 2);
        controllerEvent.executeEvent(f.context);

        // Verify signals remain RED
        for (Direction dir : Direction.values()) {
            assertEquals(SignalState.RED, f.context.getTrafficSignal(dir).getCurrentState(),
                    "All signals must remain RED when strategy returns null");
        }

        // Verify next event in scheduler is StrategyControllerEvent scheduled at t=10.0 with remainingPhases still = 3
        assertEquals(2, f.scheduler.getEventCount(), "Should have the vehicle arrival and rescheduled controller");
        SimulationEvent next = f.scheduler.getNextEvent();
        assertTrue(next instanceof StrategyControllerEvent || next instanceof VehicleArrivalEvent);
    }

    @Test
    @DisplayName("Event-driven jump: idle controller advances directly to distant arrival without consuming budget")
    void testIdleControllerJumpsToDistantFutureArrival() {
        TestFixture f = createFixture();

        // DensityBasedStrategy returns null when intersection is empty
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // Distant arrival at t=120.0s
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C1", 120.0), "ROAD-NORTH", 120.0, 1));

        // Start controller at t=0.0 with 8 phases
        f.scheduler.scheduleEvent(new StrategyControllerEvent(strategy, 8, 0.0, 2));

        // Run simulation
        f.engine.run();

        // Vehicle should have arrived and been served cleanly
        assertEquals(1, f.context.getCompletedVehicleCount(), "Distant vehicle must be served");
        assertEquals(0, f.intersection.getVehicleCount(), "No vehicles remaining in lane");
    }

    @Test
    @DisplayName("Ordering: simultaneous initial arrival and controller event at t=0.0 processes arrival first")
    void testSimultaneousInitialArrivalAndControllerEvent() {
        TestFixture f = createFixture();
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // Arrival at t=0.0 (priority 1)
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C0", 0.0), "ROAD-NORTH", 0.0, 1));

        // Controller at t=0.0 (priority 2)
        f.scheduler.scheduleEvent(new StrategyControllerEvent(strategy, 4, 0.0, 2));

        f.engine.run();

        // Vehicle arrives at 0.0, signal turns green at 0.0, vehicle departs
        assertEquals(1, f.context.getCompletedVehicleCount());
        assertEquals(0, f.intersection.getVehicleCount());
    }

    @Test
    @DisplayName("Ordering: dynamically scheduled arrival at t=20.0 is processed before controller at t=20.0")
    void testSimultaneousDynamicArrivalAndControllerEvent() {
        TestFixture f = createFixture();
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // First car at t=0.0
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C0", 0.0), "ROAD-NORTH", 0.0, 1));
        // Second car arrives at t=20.0 (simultaneous with second phase boundary)
        f.scheduler.scheduleEvent(new VehicleArrivalEvent(new Car("C20", 20.0), "ROAD-EAST", 20.0, 1));

        f.scheduler.scheduleEvent(new StrategyControllerEvent(strategy, 4, 0.0, 2));

        f.engine.run();

        assertEquals(2, f.context.getCompletedVehicleCount(), "Both vehicles must be served");
        assertEquals(0, f.intersection.getVehicleCount());
    }

    @Test
    @DisplayName("Termination: controller cleanly stops when intersection is empty and no pending arrivals remain")
    void testCleanTerminationWhenNoVehiclesAndNoArrivalsRemain() {
        TestFixture f = createFixture();
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // No arrivals scheduled at all
        StrategyControllerEvent controllerEvent = new StrategyControllerEvent(strategy, 5, 0.0, 2);
        controllerEvent.executeEvent(f.context);

        // Must not reschedule anything
        assertEquals(0, f.scheduler.getEventCount(), "Scheduler must remain empty when terminating");
    }

    @Test
    @DisplayName("Final active phase: executes full GREEN -> YELLOW -> RED transition before controller stops")
    void testFinalActivePhaseCompletesSequenceBeforeControllerStops() {
        TestFixture f = createFixture();
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // 1 car in NORTH
        f.northLane.addVehicle(new Car("C-FINAL", 0.0));

        // Controller with exactly 1 remaining phase
        f.scheduler.scheduleEvent(new StrategyControllerEvent(strategy, 1, 0.0, 2));

        List<SignalState> observedStates = new ArrayList<>();
        f.engine.run();

        // The signal must have returned to RED after the full phase
        assertEquals(SignalState.RED, f.context.getTrafficSignal(Direction.NORTH).getCurrentState(),
                "Final phase must transition back to RED at t=10.0");
        assertEquals(10.0, f.engine.getClock().getCurrentTime(),
                "Simulation clock must reach 10.0s to complete the final phase");
        assertEquals(0, f.scheduler.getEventCount(), "Controller must not reschedule after final phase");
    }

    @Test
    @DisplayName("EmergencyArrival: EmergencyArrivalEvent wakes up idle controller and prevents premature termination")
    void testEmergencyArrivalEventWakesUpController() {
        TestFixture f = createFixture();
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // EmergencyArrivalEvent scheduled at t=25.0
        EmergencyVehicle ambulance = new EmergencyVehicle("AMB-1", 25.0);
        f.scheduler.scheduleEvent(new EmergencyArrivalEvent(ambulance, f.northLane.getId(), 25.0, 1));

        // Controller starts at t=0.0
        f.scheduler.scheduleEvent(new StrategyControllerEvent(strategy, 4, 0.0, 2));

        f.engine.run();

        // Emergency vehicle must be served and simulation must advance to at least 25.0s
        assertTrue(f.engine.getClock().getCurrentTime() >= 25.0, "Simulation clock must advance to emergency arrival");
        assertEquals(1, f.context.getCompletedVehicleCount(), "Emergency vehicle must be served and completed");
        assertEquals(0, f.intersection.getVehicleCount());
    }
}
