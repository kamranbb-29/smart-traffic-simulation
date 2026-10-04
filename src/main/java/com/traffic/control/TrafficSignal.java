package com.traffic.control;

public class TrafficSignal {

    private final Direction direction;
    private SignalState currentState;

    public TrafficSignal(Direction direction) {
        this.direction = direction;
        this.currentState = SignalState.RED; // Default state
    }

    public Direction getDirection() {
        return direction;
    }

    public SignalState getCurrentState() {
        return currentState;
    }

    private boolean isValidTransition(SignalState currentState, SignalState nextState) {
        // Define valid transitions based on the current state
        switch (currentState) {
            case RED:
                return nextState == SignalState.GREEN;
            case GREEN:
                return nextState == SignalState.YELLOW;
            case YELLOW:
                return nextState == SignalState.RED;
            default:
                return false;
        }
    }

    public void transitionTo(SignalState nextState) throws InvalidSignalTransitionException {
        if (isValidTransition(currentState, nextState)) {
            currentState = nextState;
        } else {
            throw new InvalidSignalTransitionException(currentState, nextState);
        }
    }

}
