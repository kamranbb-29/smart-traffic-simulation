package com.traffic.simulation.engine;

import org.junit.jupiter.api.Test;

import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Road;
import com.traffic.simulation.engine.SimulationContext;

import static org.junit.jupiter.api.Assertions.*;
public class SimulationContextTest {
    @Test
    void getRoadReturnsCorrectRoad() {
        SimulationClock clock = new SimulationClock();
        Intersection intersection = new Intersection("INT1");
        Road road = new Road("R1");

        intersection.addRoad(road);

        SimulationContext context = new SimulationContext(clock, intersection);

        assertSame(road, context.getRoad("R1"));
    }

    @Test
    void getRoadThrowsExceptionForUnknownId() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> context.getRoad("R99")
        );
    }
}
