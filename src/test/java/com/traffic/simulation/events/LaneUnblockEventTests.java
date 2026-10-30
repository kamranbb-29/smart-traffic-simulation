
package com.traffic.simulation.events;

import com.traffic.control.Direction;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;

import com.traffic.simulation.engine.SimulationEngine;

import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LaneUnblockEventTests {

    @Test
    void shouldUnblockCorrectLane() {

        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1", Direction.NORTH);

        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        road.addLane(lane1);
        road.addLane(lane2);

        engine.getContext().getIntersection().addRoad(road);

        lane1.blockLane();
        lane2.blockLane();

        LaneUnblockEvent event = new LaneUnblockEvent(
            "R1", "L1", 10.0, 1
        );

        event.executeEvent(engine.getContext());

        assertFalse(lane1.isIndividuallyBlocked());
        assertTrue(lane2.isIndividuallyBlocked());
    }

    @Test
    void shouldNotClearRoadWideBlockage() {

        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        road.addLane(lane);
        engine.getContext().getIntersection().addRoad(road);

        road.blockRoad();
        lane.blockLane();

        new LaneUnblockEvent(
            "R1", "L1", 10.0, 1
        ).executeEvent(engine.getContext());

        assertFalse(lane.isIndividuallyBlocked());
        assertTrue(lane.isRoadWideBlocked());
        assertTrue(lane.isBlocked());
    }

    @Test
    void shouldScheduleMovementWhenLaneIsUnblockedAndSignalIsGreen() {

        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        lane.addVehicle(new com.traffic.traffic.vehicle.Car("C1", 0));

        road.addLane(lane);
        engine.getContext().getIntersection().addRoad(road);

        lane.blockLane();

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);

        try {
            signal.transitionTo(SignalState.GREEN);
        } catch (Exception exception) {
            fail("Signal setup failed");
        }

        engine.getContext().addTrafficSignal(signal);

        new LaneUnblockEvent(
            "R1", "L1", 10.0, 1
        ).executeEvent(engine.getContext());

        assertFalse(lane.isBlocked());
        assertEquals(1, engine.getScheduler().getEventCount());
    }

    @Test
    void shouldNotScheduleMovementWhenSignalIsRed() {

        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        lane.addVehicle(new com.traffic.traffic.vehicle.Car("C1", 0));

        road.addLane(lane);
        engine.getContext().getIntersection().addRoad(road);

        lane.blockLane();

        engine.getContext().addTrafficSignal(
            new TrafficSignal(Direction.NORTH)
        );

        new LaneUnblockEvent(
            "R1", "L1", 10.0, 1
        ).executeEvent(engine.getContext());

        assertFalse(lane.isBlocked());
        assertEquals(0, engine.getScheduler().getEventCount());
    }

    @Test
    void shouldRejectInvalidLane() {

        SimulationEngine engine = new SimulationEngine();

        engine.getContext().getIntersection().addRoad(
            new Road("R1", Direction.NORTH)
        );

        LaneUnblockEvent event = new LaneUnblockEvent(
            "R1", "INVALID", 10.0, 1
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> event.executeEvent(engine.getContext())
        );
    }

    @Test
    void shouldRejectNullRoadId() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new LaneUnblockEvent(
                null, "L1", 10.0, 1
            )
        );
    }

    @Test
    void shouldRejectNullLaneId() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new LaneUnblockEvent(
                "R1", null, 10.0, 1
            )
        );
    }

    @Test
    void shouldRejectNullContext() {

        LaneUnblockEvent event = new LaneUnblockEvent(
            "R1", "L1", 10.0, 1
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> event.executeEvent(null)
        );
    }
}
