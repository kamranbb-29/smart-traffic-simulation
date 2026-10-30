package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.control.Direction;
import com.traffic.control.InvalidSignalTransitionException;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;

public class TrafficLightChangeEvent extends SimulationEvent {

    private final Direction direction;
    private final SignalState nextState;

    public TrafficLightChangeEvent(
            Direction direction,
            SignalState nextState,
            double eventTime,
            int eventPriority) {

        super(eventTime, "Traffic_Light_Change", eventPriority);

        if (direction == null || nextState == null) {
            throw new IllegalArgumentException(
                "Direction and SignalState cannot be null"
            );
        }

        this.direction = direction;
        this.nextState = nextState;
    }

    public Direction getDirection() {
        return direction;
    }

    public SignalState getNextState() {
        return nextState;
    }

    @Override
    public void executeEvent(SimulationContext context) {

        if (context == null) {
            throw new IllegalArgumentException(
                "SimulationContext cannot be null"
            );
        }

        if (context.isSignalFailed(direction)) {
            System.out.println(
                "Signal " + direction + " is failed. State change skipped."
            );
            return;
        }

        TrafficSignal signal = context.getTrafficSignal(direction);

    try {
        signal.transitionTo(nextState);
    } catch (InvalidSignalTransitionException e) {
        throw new IllegalStateException(
            "Failed to change signal " + direction + " to " + nextState,
            e
        );
    }

    if (nextState == SignalState.GREEN) {
        context.scheduleMovementsForDirection(
            direction,
            getEventTime(),
            getEventPriority()
        );
    }
        System.out.println(
            "Signal " + direction + " changed to " + nextState +
            " at simulation time " + getEventTime()
        );
    }
}
