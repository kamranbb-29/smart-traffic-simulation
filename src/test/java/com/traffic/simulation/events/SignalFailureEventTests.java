package com.traffic.simulation.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.control.Direction;
import com.traffic.control.TrafficSignal;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;

public class SignalFailureEventTests {
    @Test
    void executeEventMarksSignalAsFailed() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        SignalFailureEvent event = new SignalFailureEvent(
            Direction.NORTH, 10.0, 1
        );

        event.executeEvent(context);

        assertTrue(context.isSignalFailed(Direction.NORTH));
    }

    @Test
    void executeEventWithNullContextThrowsException() {
        SignalFailureEvent event = new SignalFailureEvent(
            Direction.NORTH, 10.0, 1
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
            () -> new SignalFailureEvent(null, 10.0, 1)
        );
    }

    @Test
    void getDirectionReturnsCorrectDirection() {
        SignalFailureEvent event = new SignalFailureEvent(
            Direction.EAST, 10.0, 1
        );

        assertEquals(Direction.EAST, event.getDirection());
    }
}
