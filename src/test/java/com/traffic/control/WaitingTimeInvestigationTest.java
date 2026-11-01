package com.traffic.control;

import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.events.SimulationEvent;
import com.traffic.simulation.events.VehicleArrivalEvent;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.Vehicle;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WaitingTimeInvestigationTest {

    private static class DummyAdvanceEvent extends SimulationEvent {
        public DummyAdvanceEvent(double eventTime) {
            super(eventTime, "DummyAdvance", 1);
        }

        @Override
        public void executeEvent(SimulationContext context) {
            // No operation, just advances clock to eventTime
        }
    }

    @Test
    void testVehicleWaitingOver120SecondsStillReportsZeroWaitingTime() {
        Intersection intersection = new Intersection("INT1");
        SimulationEngine engine = new SimulationEngine(intersection);
        SimulationContext context = engine.getContext();

        // Set up NORTH road and lane
        Road northRoad = new Road("ROAD-NORTH", Direction.NORTH);
        Lane northLane = new Lane("LANE-NORTH");
        northRoad.addLane(northLane);
        intersection.addRoad(northRoad);

        // Signal is RED by default
        TrafficSignal northSignal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(northSignal);

        // Schedule CAR-1 to arrive at t = 0.0
        Vehicle car = new Car("CAR-1", 0.0);
        engine.getScheduler().scheduleEvent(
                new VehicleArrivalEvent(car, "ROAD-NORTH", 0.0, 2)
        );

        // Schedule dummy advance event at t = 150.0 to advance simulation time
        engine.getScheduler().scheduleEvent(new DummyAdvanceEvent(150.0));

        // Run simulation to t = 150.0
        engine.run();

        // 1. Verify simulation clock advanced past 120 seconds
        assertEquals(150.0, engine.getClock().getCurrentTime(), "Simulation clock should be at 150.0s");

        // 2. Verify car is still waiting in lane
        assertEquals(1, northLane.getVehicleCount(), "Car should still be in lane");
        assertSame(car, northLane.getNextVehicle());

        // 3. Inspect Vehicle.getWaitingTime()
        System.out.println("DEBUG: Vehicle.getWaitingTime() at t=150.0: " + car.getWaitingTime());
        assertEquals(0.0, car.getWaitingTime(), "Vehicle.getWaitingTime() is strictly 0.0 despite 150s elapsed");

        // 4. Inspect Intersection.getCurrentTrafficState()
        Map<Direction, DirectionTraffic> state = intersection.getCurrentTrafficState();
        double reportedWait = state.get(Direction.NORTH).getWaitingTime();
        System.out.println("DEBUG: DirectionTraffic.getWaitingTime() at t=150.0: " + reportedWait);
        assertEquals(0.0, reportedWait, 1e-6, "Reported direction waiting time is strictly 0 despite 150s elapsed");

        // 5. Inspect AdaptivePriorityStrategy behavior
        TrafficSnapshot snapshot = new TrafficSnapshot(state);
        AdaptivePriorityStrategy adaptive = new AdaptivePriorityStrategy();
        Direction chosen = adaptive.chooseDirection(snapshot);
        System.out.println("DEBUG: AdaptivePriorityStrategy chosen direction: " + chosen);

        // 6. Demonstrate that starvation check (waitingTime >= 120) fails
        assertFalse(reportedWait >= 120, "Starvation condition traffic.getWaitingTime() >= 120 cannot be met");
    }

    @Test
    void testDensityBasedStrategyTieBreakingDeterminism() {
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // 1. Inspect HashMap iteration order with identical vehicle counts across all directions
        Intersection intersection = new Intersection("INT1");
        for (Direction dir : Direction.values()) {
            Road road = new Road("ROAD-" + dir, dir);
            Lane lane = new Lane("LANE-" + dir);
            lane.addVehicle(new Car("CAR-" + dir, 0.0));
            road.addLane(lane);
            intersection.addRoad(road);
        }

        Map<Direction, DirectionTraffic> state = intersection.getCurrentTrafficState();
        TrafficSnapshot snapshot = new TrafficSnapshot(state);

        System.out.println("DEBUG: TrafficSnapshot internal map type: " + snapshot.getTrafficData().getClass().getName());
        System.out.println("DEBUG: TrafficSnapshot iteration order:");
        for (Direction dir : snapshot.getTrafficData().keySet()) {
            System.out.println("  -> " + dir + " (hash: " + dir.hashCode() + ", bucket: " + (dir.hashCode() & 15) + ")");
        }

        Direction chosen = strategy.chooseDirection(snapshot);
        System.out.println("DEBUG: DensityBasedStrategy chosen direction on 4-way tie: " + chosen);

        // Verify that the chosen direction is strictly NORTH (ordinal 0 tie-breaker)
        assertEquals(Direction.NORTH, chosen, "DensityBasedStrategy must deterministically pick NORTH on a 4-way tie");
    }

    @Test
    void testDensityBasedTieBreakingDependsOnIterationOrder() {
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // Map with NORTH first
        Map<Direction, DirectionTraffic> mapWithNorthFirst = new java.util.LinkedHashMap<>();
        mapWithNorthFirst.put(Direction.NORTH, new DirectionTraffic(5, 0, 0));
        mapWithNorthFirst.put(Direction.SOUTH, new DirectionTraffic(5, 0, 0));

        // When passed to TrafficSnapshot, it wraps into HashMap
        TrafficSnapshot snapshot1 = new TrafficSnapshot(mapWithNorthFirst);
        Direction chosen1 = strategy.chooseDirection(snapshot1);

        // Map with SOUTH first
        Map<Direction, DirectionTraffic> mapWithSouthFirst = new java.util.LinkedHashMap<>();
        mapWithSouthFirst.put(Direction.SOUTH, new DirectionTraffic(5, 0, 0));
        mapWithSouthFirst.put(Direction.NORTH, new DirectionTraffic(5, 0, 0));
        TrafficSnapshot snapshot2 = new TrafficSnapshot(mapWithSouthFirst);
        Direction chosen2 = strategy.chooseDirection(snapshot2);

        System.out.println("DEBUG: snapshot1 chosen: " + chosen1 + ", snapshot2 chosen: " + chosen2);
        assertEquals(Direction.NORTH, chosen1, "snapshot1 must select NORTH");
        assertEquals(Direction.NORTH, chosen2, "snapshot2 must select NORTH");
        assertEquals(chosen1, chosen2, "Both snapshots must deterministically yield NORTH regardless of map order");
    }
}
