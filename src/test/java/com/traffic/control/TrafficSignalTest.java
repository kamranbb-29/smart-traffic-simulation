package com.traffic.control;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TrafficSignalTest {

    @Test
    void initialStateIsRed() {
        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        assertEquals(SignalState.RED, signal.getCurrentState());
    }

    @Test
    void redCanTransitionToGreen() throws InvalidSignalTransitionException {
        // Arrange
        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        // Act
        signal.transitionTo(SignalState.GREEN);
        // Assert
        assertEquals(SignalState.GREEN, signal.getCurrentState());
    }

    @Test
    void yellowCanTransitionToRed() throws InvalidSignalTransitionException {
        // Arrange
        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);
        signal.transitionTo(SignalState.YELLOW);
        // Act
        signal.transitionTo(SignalState.RED);
        // Assert
        assertEquals(SignalState.RED, signal.getCurrentState());
    }

    @Test
    void greenCanTransitionToYellow() throws InvalidSignalTransitionException {
        // Arrange
        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);
        // Act
        signal.transitionTo(SignalState.YELLOW);
        // Assert
        assertEquals(SignalState.YELLOW, signal.getCurrentState());
    }

    @Test
    void greenCannotTransitionToRed() throws InvalidSignalTransitionException {
        // Arrange
        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);

        // Act + Assert
        assertThrows(InvalidSignalTransitionException.class, () -> signal.transitionTo(SignalState.RED));
        // ...
    }
}