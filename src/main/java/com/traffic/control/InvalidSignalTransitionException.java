package com.traffic.control;

public class InvalidSignalTransitionException extends Exception {
    public InvalidSignalTransitionException(SignalState currentState, SignalState nextState) {
        super("Invalid transition from " + currentState + " to " + nextState);
    }
}
