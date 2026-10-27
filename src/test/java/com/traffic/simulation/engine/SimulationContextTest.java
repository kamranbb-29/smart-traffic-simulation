package com.traffic.simulation.engine;

import org.junit.jupiter.api.Test;

import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Road;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.control.Direction;
import com.traffic.control.TrafficSignal;

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

    @Test
    void addTrafficSignalAndRetrieveIt() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);

        context.addTrafficSignal(signal);

        assertSame(signal, context.getTrafficSignal(Direction.NORTH));
    }

    @Test
    void addDuplicateTrafficSignalThrowsException() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal firstSignal = new TrafficSignal(Direction.NORTH);
        TrafficSignal secondSignal = new TrafficSignal(Direction.NORTH);

        context.addTrafficSignal(firstSignal);

        assertThrows(
            IllegalArgumentException.class,
            () -> context.addTrafficSignal(secondSignal)
        );
    }

    @Test
    void getTrafficSignalThrowsExceptionForUnknownDirection() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> context.getTrafficSignal(Direction.NORTH)
        );
    }

    @Test
    void addTrafficSignalWithNullDirectionThrowsException() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> context.addTrafficSignal(signal)
        );
    }

    @Test
    void failSignalMarksSignalAsFailed() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        context.failSignal(Direction.NORTH);

        assertTrue(context.isSignalFailed(Direction.NORTH));
    }

    @Test
    void recoverSignalRemovesFailedStatus() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);
        context.failSignal(Direction.NORTH);

        context.recoverSignal(Direction.NORTH);

        assertFalse(context.isSignalFailed(Direction.NORTH));
    }

    @Test
    void isSignalFailedThrowsExceptionForUnknownDirection() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> context.isSignalFailed(Direction.NORTH)
        );
    }
}
