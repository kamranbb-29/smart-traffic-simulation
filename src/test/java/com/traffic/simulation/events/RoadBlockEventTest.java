package com.traffic.simulation.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.simulation.events.RoadBlockEvent;
import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.traffic.road.*;

public class RoadBlockEventTest {
    @Test
    void testRoadBlockBlocksAllLanes(){
        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1");
        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        road.addLane(lane1);
        road.addLane(lane2);
        engine.getContext().getIntersection().addRoad(road);

        RoadBlockEvent event = new RoadBlockEvent("R1", 5.0, 1);
        event.executeEvent(engine.getContext());

        assertTrue(road.isBlocked());
        assertTrue(lane1.isBlocked());
        assertTrue(lane2.isBlocked());
    }

    @Test 
    void testOtherRoadsRemainUnblocked(){
        SimulationEngine engine = new SimulationEngine();

        Road road1 = new Road("R1");
        Road road2 = new Road("R2");
        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        road1.addLane(lane1);
        road2.addLane(lane2);

        engine.getContext().getIntersection().addRoad(road1);
        engine.getContext().getIntersection().addRoad(road2);

        RoadBlockEvent event = new RoadBlockEvent("R1", 5.0, 1);
        event.executeEvent(engine.getContext());

        assertTrue(road1.isBlocked());
        assertFalse(road2.isBlocked());
        assertFalse(lane2.isBlocked());
    }

    @Test 
    void AccidentBlockRemainsBlockedAfterUnblock(){
        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1");
        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        road.addLane(lane1);
        road.addLane(lane2);

        engine.getContext().getIntersection().addRoad(road);

        lane1.blockLane();

        RoadBlockEvent event1 = new RoadBlockEvent("R1", 5.0, 1);
        event1.executeEvent(engine.getContext());

        RoadUnblockEvent event2 = new RoadUnblockEvent("R1", 10.0, 1);
        event2.executeEvent(engine.getContext());

        assertFalse(road.isBlocked());
        assertFalse(lane2.isBlocked());
        assertTrue(lane1.isBlocked());
    }

    @Test
    void testInvalidRoadId(){
        SimulationEngine engine = new SimulationEngine();

        RoadBlockEvent event = new RoadBlockEvent("R1", 5.0, 1);

        assertThrows(IllegalArgumentException.class, () -> event.executeEvent(engine.getContext()));
    }
}
