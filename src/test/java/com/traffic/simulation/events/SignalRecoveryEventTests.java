package com.traffic.simulation.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.control.Direction;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;
import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.engine.SimulationEngine;

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

    @Test
    void shouldScheduleMovementWhenGreenSignalRecovers()
            throws Exception {

        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        lane.addVehicle(new Car("C1", 0));
        road.addLane(lane);

        engine.getContext().getIntersection().addRoad(road);

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);

        engine.getContext().addTrafficSignal(signal);
        engine.getContext().failSignal(Direction.NORTH);

        SignalRecoveryEvent event = new SignalRecoveryEvent(
            Direction.NORTH, 10.0, 1
        );

        event.executeEvent(engine.getContext());

        assertFalse(engine.getContext().isSignalFailed(Direction.NORTH));
        assertEquals(1, engine.getScheduler().getEventCount());
    }
}
