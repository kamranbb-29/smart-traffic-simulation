package com.traffic.traffic.vehicle;

import com.traffic.control.*;
import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.events.MovementType;
import com.traffic.simulation.events.VehicleMovementEvent;
import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused unit tests for the simulation waiting-time model:
 * dynamic queue waiting time, single-time finalization upon dequeue,
 * fractional precision, timestamp validation, starvation boundary behavior,
 * and legacy backwards-compatibility.
 */
class VehicleWaitingTimeModelTest {

    @Test
    @DisplayName("Fractional precision: dynamic wait at t=4.75 and finalized wait at t=4.75 is 3.50s")
    void testFractionalPrecisionDynamicAndFinalizedWait() {
        Vehicle vehicle = new Car("V1", 1.25);

        // Before finalization, dynamic query reflects elapsed simulation time
        assertEquals(3.50, vehicle.getWaitingTime(4.75), 1e-9, "Dynamic wait should be exactly 4.75 - 1.25 = 3.50s");
        assertEquals(0.0, vehicle.getWaitingTime(), 1e-9, "Static wait remains 0.0 before finalization");
        assertFalse(vehicle.isWaitingTimeFinalized(), "Vehicle wait should not be finalized initially");

        // Finalize upon movement at t=4.75
        vehicle.finalizeWaitingTime(4.75);
        assertTrue(vehicle.isWaitingTimeFinalized(), "Vehicle wait should be marked as finalized");
        assertEquals(3.50, vehicle.getWaitingTime(), 1e-9, "Finalized wait must be 3.50s");

        // After finalization, dynamic queries at later times must return the frozen finalized value
        assertEquals(3.50, vehicle.getWaitingTime(10.0), 1e-9, "Dynamic wait after finalization must remain frozen");
        assertEquals(3.50, vehicle.getWaitingTime(100.0), 1e-9, "Dynamic wait after finalization must remain frozen");
    }

    @Test
    @DisplayName("Zero-wait: vehicle moving immediately upon arrival incurs exactly 0.0s wait")
    void testZeroWaitWhenMovedAtArrival() {
        Vehicle vehicle = new Car("V0", 10.0);

        assertEquals(0.0, vehicle.getWaitingTime(10.0), 1e-9, "Dynamic wait at arrival instant is 0.0");
        vehicle.finalizeWaitingTime(10.0);

        assertTrue(vehicle.isWaitingTimeFinalized());
        assertEquals(0.0, vehicle.getWaitingTime(), 1e-9, "Finalized wait for immediate movement must be 0.0");
    }

    @Test
    @DisplayName("Idempotent finalization: repeated finalize calls do not double-count or alter wait")
    void testIdempotentFinalization() {
        Vehicle vehicle = new Car("V-IDEM", 5.0);

        vehicle.finalizeWaitingTime(8.0); // 8.0 - 5.0 = 3.0s
        assertEquals(3.0, vehicle.getWaitingTime(), 1e-9);

        // Subsequent valid finalizations must be no-ops
        vehicle.finalizeWaitingTime(12.0);
        assertEquals(3.0, vehicle.getWaitingTime(), 1e-9, "Second finalization must not change finalized wait");

        vehicle.finalizeWaitingTime(8.0);
        assertEquals(3.0, vehicle.getWaitingTime(), 1e-9, "Third finalization must not change finalized wait");
    }

    @Test
    @DisplayName("Timestamp validation: negative, NaN, infinity, and movement < arrival are rejected")
    void testTimestampValidation() {
        // Constructor validation
        assertThrows(IllegalArgumentException.class, () -> new Car("BAD1", -0.01));
        assertThrows(IllegalArgumentException.class, () -> new Car("BAD2", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Car("BAD3", Double.POSITIVE_INFINITY));

        Vehicle vehicle = new Car("V-VAL", 10.0);

        // Movement before arrival rejected
        assertThrows(IllegalArgumentException.class, () -> vehicle.finalizeWaitingTime(9.99));

        // Invalid movement timestamps rejected
        assertThrows(IllegalArgumentException.class, () -> vehicle.finalizeWaitingTime(-1.0));
        assertThrows(IllegalArgumentException.class, () -> vehicle.finalizeWaitingTime(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> vehicle.finalizeWaitingTime(Double.POSITIVE_INFINITY));

        // Invalid dynamic query timestamps rejected
        assertThrows(IllegalArgumentException.class, () -> vehicle.getWaitingTime(-0.1));
        assertThrows(IllegalArgumentException.class, () -> vehicle.getWaitingTime(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> vehicle.getWaitingTime(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> vehicle.getWaitingTime(Double.NEGATIVE_INFINITY));

        // Dynamic query before vehicle arrival clamps wait to 0.0 (no negative wait)
        assertEquals(0.0, vehicle.getWaitingTime(5.0), 1e-9, "Query prior to arrival must not produce negative wait");

        // Invalid addWaitingTime increments rejected
        assertThrows(IllegalArgumentException.class, () -> vehicle.addWaitingTime(-1.0));
        assertThrows(IllegalArgumentException.class, () -> vehicle.addWaitingTime(Double.NaN));

        // Finalize vehicle validly
        vehicle.finalizeWaitingTime(15.0); // wait = 5.0

        // Validation must run before idempotency guard: invalid timestamps must still throw
        assertThrows(IllegalArgumentException.class, () -> vehicle.finalizeWaitingTime(-5.0));
        assertThrows(IllegalArgumentException.class, () -> vehicle.finalizeWaitingTime(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> vehicle.finalizeWaitingTime(9.0)); // movement < arrival
        assertEquals(5.0, vehicle.getWaitingTime(), 1e-9, "Valid wait remains unchanged after rejected attempts");

        // Dynamic query must still validate and reject invalid timestamps after finalization
        assertThrows(IllegalArgumentException.class, () -> vehicle.getWaitingTime(-1.0));
        assertThrows(IllegalArgumentException.class, () -> vehicle.getWaitingTime(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> vehicle.getWaitingTime(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> vehicle.getWaitingTime(Double.NEGATIVE_INFINITY));
    }

    @Test
    @DisplayName("State integrity: addWaitingTime after finalization throws IllegalStateException and preserves wait")
    void testAddWaitingTimeAfterFinalizationThrowsIllegalStateException() {
        Vehicle vehicle = new Car("V-POST", 5.0);
        vehicle.finalizeWaitingTime(15.0); // finalized wait = 10.0s

        // Attempting to add waiting time to a finalized vehicle must throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> vehicle.addWaitingTime(5.0));
        assertEquals(10.0, vehicle.getWaitingTime(), 1e-9, "Finalized wait must remain strictly frozen at 10.0s");
        assertEquals(10.0, vehicle.getWaitingTime(20.0), 1e-9, "Dynamic query must also remain frozen at 10.0s");

        // Input validation occurs before checking finalization state
        assertThrows(IllegalArgumentException.class, () -> vehicle.addWaitingTime(-1.0));
        assertThrows(IllegalArgumentException.class, () -> vehicle.addWaitingTime(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> vehicle.addWaitingTime(Double.POSITIVE_INFINITY));
        assertEquals(10.0, vehicle.getWaitingTime(), 1e-9, "Finalized wait remains unchanged after rejected increments");
    }

    @Test
    @DisplayName("Legacy compatibility: externally added baseline wait combines with dynamic queue wait")
    void testLegacyCompatibilityWithAddWaitingTime() {
        Vehicle vehicle = new Car("V-LEGACY", 5.0);
        vehicle.addWaitingTime(10.0); // e.g. upstream link wait

        // At t=20.0, dynamic wait is 10.0 baseline + (20.0 - 5.0 queue wait) = 25.0
        assertEquals(25.0, vehicle.getWaitingTime(20.0), 1e-9);

        // Finalize at t=20.0
        vehicle.finalizeWaitingTime(20.0);
        assertEquals(25.0, vehicle.getWaitingTime(), 1e-9);
        assertEquals(25.0, vehicle.getWaitingTime(30.0), 1e-9);
    }

    @Test
    @DisplayName("Event execution: paused movement (RED light or blocked road) does NOT finalize waiting time")
    void testPausedMovementDoesNotFinalizeWait() throws Exception {
        Intersection intersection = new Intersection("INT-PAUSE");
        Road road = new Road("ROAD-N", Direction.NORTH);
        Lane lane = new Lane("LANE-N", MovementType.STRAIGHT);
        road.addLane(lane);
        intersection.addRoad(road);

        Vehicle vehicle = new Car("V-PAUSE", 2.0, MovementType.STRAIGHT);
        lane.addVehicle(vehicle);

        EventScheduler scheduler = new EventScheduler();
        SimulationClock clock = new SimulationClock();
        SimulationContext context = new SimulationContext(clock, intersection, scheduler, 1.0);

        // TrafficSignal default state is RED
        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        // Movement event at t=10.0
        VehicleMovementEvent movementEvent = new VehicleMovementEvent(
                "ROAD-N", "LANE-N", Direction.NORTH, MovementType.STRAIGHT, 10.0, 1
        );

        movementEvent.executeEvent(context);

        // Movement paused due to RED light
        assertEquals(1, lane.getVehicleCount(), "Vehicle must still be in lane");
        assertSame(vehicle, lane.getNextVehicle());
        assertFalse(vehicle.isWaitingTimeFinalized(), "Vehicle waiting time must NOT be finalized on paused movement");
        assertEquals(0.0, vehicle.getWaitingTime(), 1e-9, "Static waiting time remains 0.0 while in queue");

        // Dynamic query at t=10.0 still works
        assertEquals(8.0, vehicle.getWaitingTime(10.0), 1e-9, "Dynamic wait at t=10.0 is 10.0 - 2.0 = 8.0s");

        // Now test blocked road under GREEN signal
        signal.transitionTo(SignalState.GREEN);
        road.blockRoad();
        movementEvent.executeEvent(context);

        // Still paused due to blocked road
        assertEquals(1, lane.getVehicleCount());
        assertFalse(vehicle.isWaitingTimeFinalized(), "Vehicle wait must NOT be finalized while road is blocked");
    }

    @Test
    @DisplayName("Event execution: successful movement finalizes waiting time exactly once upon dequeue")
    void testSuccessfulMovementFinalizesWaitOnDequeue() throws Exception {
        Intersection intersection = new Intersection("INT-SUCCESS");
        Road road = new Road("ROAD-N", Direction.NORTH);
        Lane lane = new Lane("LANE-N", MovementType.STRAIGHT);
        road.addLane(lane);
        intersection.addRoad(road);

        Vehicle vehicle = new Car("V-MOVE", 5.0, MovementType.STRAIGHT);
        lane.addVehicle(vehicle);

        EventScheduler scheduler = new EventScheduler();
        SimulationClock clock = new SimulationClock();
        SimulationContext context = new SimulationContext(clock, intersection, scheduler, 1.0);

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);
        context.addTrafficSignal(signal);

        // Execute successful movement at t=17.5
        VehicleMovementEvent movementEvent = new VehicleMovementEvent(
                "ROAD-N", "LANE-N", Direction.NORTH, MovementType.STRAIGHT, 17.5, 1
        );

        movementEvent.executeEvent(context);

        // Vehicle successfully dequeued
        assertEquals(0, lane.getVehicleCount(), "Lane should be empty after successful movement");
        assertTrue(vehicle.isWaitingTimeFinalized(), "Waiting time must be finalized on successful dequeue");
        assertEquals(12.5, vehicle.getWaitingTime(), 1e-9, "Finalized wait is exactly 17.5 - 5.0 = 12.5s");
        assertEquals(12.5, vehicle.getWaitingTime(50.0), 1e-9, "Dynamic wait remains frozen at 12.5s");
    }

    @Test
    @DisplayName("DirectionTraffic and Intersection: total vs maximum waiting time calculation")
    void testTotalAndMaxWaitingTimeInTrafficState() {
        Intersection intersection = new Intersection("INT-STATE");
        Road roadNorth = new Road("ROAD-N", Direction.NORTH);
        Lane laneNorth = new Lane("LANE-N", MovementType.STRAIGHT);
        roadNorth.addLane(laneNorth);
        intersection.addRoad(roadNorth);

        Vehicle v1 = new Car("V1", 0.0);  // at t=10.0, wait = 10.0
        Vehicle v2 = new Car("V2", 6.0);  // at t=10.0, wait = 4.0
        laneNorth.addVehicle(v1);
        laneNorth.addVehicle(v2);

        // Dynamic state at t=10.0
        Map<Direction, DirectionTraffic> stateAt10 = intersection.getCurrentTrafficState(10.0);
        DirectionTraffic northTraffic = stateAt10.get(Direction.NORTH);

        assertNotNull(northTraffic);
        assertEquals(2, northTraffic.getVehicleCount());
        assertEquals(0, northTraffic.getEmergencyVehicleCount());
        assertEquals(14.0, northTraffic.getTotalWaitingTime(), 1e-9, "Total wait: 10.0 + 4.0 = 14.0s");
        assertEquals(14.0, northTraffic.getWaitingTime(), 1e-9, "getWaitingTime() aliases total wait");
        assertEquals(10.0, northTraffic.getMaxWaitingTime(), 1e-9, "Max wait: max(10.0, 4.0) = 10.0s");

        // Legacy no-arg state (static wait is 0 for unfinalized vehicles)
        Map<Direction, DirectionTraffic> legacyState = intersection.getCurrentTrafficState();
        DirectionTraffic legacyNorth = legacyState.get(Direction.NORTH);
        assertEquals(0.0, legacyNorth.getTotalWaitingTime(), 1e-9);
        assertEquals(0.0, legacyNorth.getMaxWaitingTime(), 1e-9);

        // DirectionTraffic constructor validation
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(2, 0, 10.0, 15.0),
                "Max wait cannot exceed total wait");
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(-1, 0, 10.0, 5.0));
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(1, 2, 10.0, 5.0));
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(1, 0, -1.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(1, 0, 10.0, -1.0));
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(1, 0, Double.NaN, 0.0));

        // 3-argument constructor backwards-compatibility
        DirectionTraffic compatTraffic = new DirectionTraffic(3, 1, 15.0);
        assertEquals(15.0, compatTraffic.getTotalWaitingTime(), 1e-9);
        assertEquals(15.0, compatTraffic.getMaxWaitingTime(), 1e-9);
    }

    @Test
    @DisplayName("AdaptivePriorityStrategy starvation boundary: 119.9s no trigger, 120.0s triggers, 120.1s triggers")
    void testStarvationBoundaryBehaviorInAdaptiveStrategy() {
        AdaptivePriorityStrategy strategy = new AdaptivePriorityStrategy();

        // Case 1: Max wait 119.9s on NORTH (1 vehicle), SOUTH has 200 vehicles (max wait 10s)
        // Score NORTH: 119.9 + 1 + 0.5*119.9 = 180.85
        // Score SOUTH: 10.0 + 200 + 0.5*10.0 = 215.0
        // Starvation does NOT trigger (< 120.0s), so SOUTH wins by priority score
        Map<Direction, DirectionTraffic> map1 = new EnumMap<>(Direction.class);
        map1.put(Direction.NORTH, new DirectionTraffic(1, 0, 119.9, 119.9));
        map1.put(Direction.SOUTH, new DirectionTraffic(200, 0, 10.0, 10.0));
        assertEquals(Direction.SOUTH, strategy.chooseDirection(new TrafficSnapshot(map1)),
                "119.9s must not trigger starvation rule; SOUTH should win by priority score");

        // Case 2: Max wait 120.0s on NORTH (1 vehicle), SOUTH has 200 vehicles (max wait 10s)
        // Starvation rule TRIGGERS (>= 120.0s), so NORTH is selected immediately
        Map<Direction, DirectionTraffic> map2 = new EnumMap<>(Direction.class);
        map2.put(Direction.NORTH, new DirectionTraffic(1, 0, 120.0, 120.0));
        map2.put(Direction.SOUTH, new DirectionTraffic(200, 0, 10.0, 10.0));
        assertEquals(Direction.NORTH, strategy.chooseDirection(new TrafficSnapshot(map2)),
                "120.0s exactly must trigger starvation rule; NORTH should win despite lower score");

        // Case 3: Max wait 120.1s on NORTH (1 vehicle), SOUTH has 200 vehicles
        // Starvation rule TRIGGERS
        Map<Direction, DirectionTraffic> map3 = new EnumMap<>(Direction.class);
        map3.put(Direction.NORTH, new DirectionTraffic(1, 0, 120.1, 120.1));
        map3.put(Direction.SOUTH, new DirectionTraffic(200, 0, 10.0, 10.0));
        assertEquals(Direction.NORTH, strategy.chooseDirection(new TrafficSnapshot(map3)),
                "120.1s must trigger starvation rule");

        // Case 4: Multiple starving directions - highest max waiting time wins
        Map<Direction, DirectionTraffic> map4 = new EnumMap<>(Direction.class);
        map4.put(Direction.NORTH, new DirectionTraffic(5, 0, 300.0, 130.0));
        map4.put(Direction.EAST, new DirectionTraffic(1, 0, 150.0, 150.0));
        assertEquals(Direction.EAST, strategy.chooseDirection(new TrafficSnapshot(map4)),
                "Between multiple starving directions, higher max waiting time (150 > 130) must win");

        // Case 5: Multiple starving directions with equal max waiting time - tie-break by ordinal
        Map<Direction, DirectionTraffic> map5 = new EnumMap<>(Direction.class);
        map5.put(Direction.EAST, new DirectionTraffic(1, 0, 140.0, 140.0));
        map5.put(Direction.NORTH, new DirectionTraffic(1, 0, 140.0, 140.0));
        assertEquals(Direction.NORTH, strategy.chooseDirection(new TrafficSnapshot(map5)),
                "Equal starvation wait must tie-break by lower direction ordinal (NORTH < EAST)");
    }

    @Test
    @DisplayName("PriorityCalculator: calculates score with fractional waiting times without truncation")
    void testPriorityCalculatorFractionalPrecision() {
        PriorityCalculator calculator = new PriorityCalculator();
        DirectionTraffic traffic = new DirectionTraffic(3, 0, 15.5, 10.0);

        // Expected score: waitingTime (15.5) + vehicleCount (3) + 0.5 * waitingTime (7.75) = 26.25
        double score = calculator.calculatePriorityScore(traffic);
        assertEquals(26.25, score, 1e-9, "Priority score must preserve fractional waiting time precision");
    }
}
