package com.traffic.simulation.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.control.Direction;
import com.traffic.control.TrafficSignal;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;

public class SignalRecoveryEventTests {
    @Test
    void executeEventRecoversFailedSignal() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        context.failSignal(Direction.NORTH);

        SignalRecoveryEvent event = new SignalRecoveryEvent(
            Direction.NORTH, 20.0, 1
        );

        event.executeEvent(context);

        assertFalse(context.isSignalFailed(Direction.NORTH));
    }

    @Test
    void executeEventWithNullContextThrowsException() {
        SignalRecoveryEvent event = new SignalRecoveryEvent(
            Direction.NORTH, 20.0, 1
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
            () -> new SignalRecoveryEvent(null, 20.0, 1)
        );
    }

    @Test
    void getDirectionReturnsCorrectDirection() {
        SignalRecoveryEvent event = new SignalRecoveryEvent(
            Direction.SOUTH, 20.0, 1
        );

        assertEquals(Direction.SOUTH, event.getDirection());
    }
}
