package com.traffic.simulation.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.control.Direction;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.control.InvalidSignalTransitionException;

public class TrafficLightChangeEventTests {
    @Test
    void executeEventChangesRedToGreen(){
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        TrafficLightChangeEvent event = new TrafficLightChangeEvent(
            Direction.NORTH,
            SignalState.GREEN,
            10.0,
            1
        );

        event.executeEvent(context);

        assertEquals(SignalState.GREEN, signal.getCurrentState());
    }

    @Test
    void executeEventChangesGreenToYellow() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        try {
            signal.transitionTo(SignalState.GREEN);
        } catch (InvalidSignalTransitionException e) {
            fail("Setup failed: " + e.getMessage());
        }

        TrafficLightChangeEvent event = new TrafficLightChangeEvent(
            Direction.NORTH,
            SignalState.YELLOW,
            20.0,
            1
        );

        event.executeEvent(context);

        assertEquals(SignalState.YELLOW, signal.getCurrentState());
    }

    @Test
    void executeEventChangesYellowToRed() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        try {
            signal.transitionTo(SignalState.GREEN);
            signal.transitionTo(SignalState.YELLOW);
        } catch (InvalidSignalTransitionException e) {
            fail("Setup failed: " + e.getMessage());
        }

        TrafficLightChangeEvent event = new TrafficLightChangeEvent(
            Direction.NORTH,
            SignalState.RED,
            30.0,
            1
        );

        event.executeEvent(context);

        assertEquals(SignalState.RED, signal.getCurrentState());
    }

    @Test
    void executeEventWithInvalidTransitionThrowsException() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        TrafficLightChangeEvent event = new TrafficLightChangeEvent(
            Direction.NORTH,
            SignalState.YELLOW,
            10.0,
            1
        );

        assertThrows(
            IllegalStateException.class,
            () -> event.executeEvent(context)
        );

        assertEquals(SignalState.RED, signal.getCurrentState());
    }

    @Test
    void executeEventSkipsChangeWhenSignalHasFailed() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        context.failSignal(Direction.NORTH);

        TrafficLightChangeEvent event = new TrafficLightChangeEvent(
            Direction.NORTH,
            SignalState.GREEN,
            10.0,
            1
        );

        event.executeEvent(context);

        assertEquals(SignalState.RED, signal.getCurrentState());
    }

    @Test
    void executeEventWithNullContextThrowsException() {
        TrafficLightChangeEvent event = new TrafficLightChangeEvent(
            Direction.NORTH,
            SignalState.GREEN,
            10.0,
            1
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> event.executeEvent(null)
        );
    }

    @Test
    void constructorWithNullDirectionThrowsException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new TrafficLightChangeEvent(
                null, SignalState.GREEN, 10.0, 1
            )
        );
    }

    @Test
    void constructorWithNullNextStateThrowsException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new TrafficLightChangeEvent(
                Direction.NORTH, null, 10.0, 1
            )
        );
    }

    @Test
    void getDirectionReturnsCorrectDirection() {
        TrafficLightChangeEvent event = new TrafficLightChangeEvent(
            Direction.EAST,
            SignalState.GREEN,
            10.0,
            1
        );

        assertEquals(Direction.EAST, event.getDirection());
    }

    @Test
    void getNextStateReturnsCorrectState() {
        TrafficLightChangeEvent event = new TrafficLightChangeEvent(
            Direction.SOUTH,
            SignalState.YELLOW,
            10.0,
            1
        );

        assertEquals(SignalState.YELLOW, event.getNextState());
    }
}
