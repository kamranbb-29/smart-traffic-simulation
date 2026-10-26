package com.traffic.simulation.events;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;

public class AccidentEventTests {

    @Test
    void testAccidentBlocksCorrectLane() {
        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1");
        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        road.addLane(lane1);
        road.addLane(lane2);
        engine.getContext().getIntersection().addRoad(road);

        AccidentEvent event = new AccidentEvent("L1", 5.0, 1);
        event.executeEvent(engine.getContext());

        assertTrue(lane1.isBlocked());
        assertFalse(lane2.isBlocked());
    }

    @Test
    void testAccidentWithInvalidLaneId() {
        SimulationEngine engine = new SimulationEngine();

        AccidentEvent event = new AccidentEvent("INVALID", 5.0, 1);

        assertThrows(
            IllegalArgumentException.class,
            () -> event.executeEvent(engine.getContext())
        );
    }

    @Test
    void testNullLaneId() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new AccidentEvent(null, 5.0, 1)
        );
    }
}