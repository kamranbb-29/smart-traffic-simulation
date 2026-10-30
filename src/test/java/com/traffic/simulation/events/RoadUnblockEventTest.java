package com.traffic.simulation.events;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.traffic.control.Direction;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;
import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;

public class RoadUnblockEventTest {

    @Test
    void testRoadUnblock() {
        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1");
        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        road.addLane(lane1);
        road.addLane(lane2);
        engine.getContext().getIntersection().addRoad(road);

        new RoadBlockEvent("R1", 5.0, 1)
            .executeEvent(engine.getContext());

        new RoadUnblockEvent("R1", 10.0, 1)
            .executeEvent(engine.getContext());

        assertFalse(road.isBlocked());
        assertFalse(lane1.isBlocked());
        assertFalse(lane2.isBlocked());
    }

    @Test
    void testAccidentBlockRemainsAfterRoadUnblock() {
        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1");
        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        road.addLane(lane1);
        road.addLane(lane2);
        engine.getContext().getIntersection().addRoad(road);

        lane1.blockLane();

        new RoadBlockEvent("R1", 5.0, 1)
            .executeEvent(engine.getContext());

        new RoadUnblockEvent("R1", 10.0, 1)
            .executeEvent(engine.getContext());

        assertFalse(road.isBlocked());
        assertTrue(lane1.isBlocked());
        assertFalse(lane2.isBlocked());
    }

    @Test
    void testInvalidRoadId() {
        SimulationEngine engine = new SimulationEngine();

        RoadUnblockEvent event =
            new RoadUnblockEvent("INVALID", 10.0, 1);

        assertThrows(
            IllegalArgumentException.class,
            () -> event.executeEvent(engine.getContext())
        );
    }

    @Test
    void shouldScheduleMovementAfterRoadIsUnblocked()
            throws Exception {

        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        lane.addVehicle(new Car("C1", 0));
        road.addLane(lane);

        engine.getContext().getIntersection().addRoad(road);

        road.blockRoad();

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);

        engine.getContext().addTrafficSignal(signal);

        RoadUnblockEvent event = new RoadUnblockEvent(
            "R1", 10.0, 1
        );

        event.executeEvent(engine.getContext());

        assertFalse(road.isBlocked());
        assertEquals(1, engine.getScheduler().getEventCount());
    }
}
