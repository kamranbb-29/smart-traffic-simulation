package com.traffic.simulation.events;

import com.traffic.control.Direction;

public enum MovementType {
    LEFT,
    STRAIGHT,
    RIGHT;
    public Direction getExitDirection(Direction approach) {
        if (approach == null) {
            throw new IllegalArgumentException("Approach direction cannot be null");
        }

        return switch (this) {
            case LEFT -> switch (approach) {
                case NORTH -> Direction.EAST;
                case EAST -> Direction.SOUTH;
                case SOUTH -> Direction.WEST;
                case WEST -> Direction.NORTH;
            };

            case STRAIGHT -> switch (approach) {
                case NORTH -> Direction.SOUTH;
                case EAST -> Direction.WEST;
                case SOUTH -> Direction.NORTH;
                case WEST -> Direction.EAST;
            };

            case RIGHT -> switch (approach) {
                case NORTH -> Direction.WEST;
                case EAST -> Direction.NORTH;
                case SOUTH -> Direction.EAST;
                case WEST -> Direction.SOUTH;
            };
        };
    }
}
