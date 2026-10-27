package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.control.Direction;

public class SignalFailureEvent extends SimulationEvent {

    private final Direction direction;

    public SignalFailureEvent(Direction direction, double eventTime, int eventPriority) {
        super(eventTime, "Signal_Failure", eventPriority);

        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }

        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public void executeEvent(SimulationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("SimulationContext cannot be null");
        }

        context.failSignal(direction);

        System.out.println(
            "Traffic signal " + direction +
            " failed at simulation time " + getEventTime()
        );
    }
}