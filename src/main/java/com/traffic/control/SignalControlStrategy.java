package com.traffic.control;

public interface SignalControlStrategy {
    /**
     * Chooses the direction to receive a green phase.
     *
     * @param snapshot the current traffic snapshot; must not be null
     * @return the selected Direction to receive green, or null if no direction
     *         needs service / has traffic (indicating an all-red idle state).
     * @throws IllegalArgumentException if snapshot is null
     */
    Direction chooseDirection(TrafficSnapshot snapshot);

}
