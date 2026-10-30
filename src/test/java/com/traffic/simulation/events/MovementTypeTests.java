package com.traffic.simulation.events;

import com.traffic.control.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementTypeTest {

    @Test
    void northApproachExitDirections() {
        assertEquals(Direction.EAST,
            MovementType.LEFT.getExitDirection(Direction.NORTH));

        assertEquals(Direction.SOUTH,
            MovementType.STRAIGHT.getExitDirection(Direction.NORTH));

        assertEquals(Direction.WEST,
            MovementType.RIGHT.getExitDirection(Direction.NORTH));
    }

    @Test
    void eastApproachExitDirections() {
        assertEquals(Direction.SOUTH,
            MovementType.LEFT.getExitDirection(Direction.EAST));

        assertEquals(Direction.WEST,
            MovementType.STRAIGHT.getExitDirection(Direction.EAST));

        assertEquals(Direction.NORTH,
            MovementType.RIGHT.getExitDirection(Direction.EAST));
    }

    @Test
    void southApproachExitDirections() {
        assertEquals(Direction.WEST,
            MovementType.LEFT.getExitDirection(Direction.SOUTH));

        assertEquals(Direction.NORTH,
            MovementType.STRAIGHT.getExitDirection(Direction.SOUTH));

        assertEquals(Direction.EAST,
            MovementType.RIGHT.getExitDirection(Direction.SOUTH));
    }

    @Test
    void westApproachExitDirections() {
        assertEquals(Direction.NORTH,
            MovementType.LEFT.getExitDirection(Direction.WEST));

        assertEquals(Direction.EAST,
            MovementType.STRAIGHT.getExitDirection(Direction.WEST));

        assertEquals(Direction.SOUTH,
            MovementType.RIGHT.getExitDirection(Direction.WEST));
    }

    @Test
    void nullApproachThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> MovementType.LEFT.getExitDirection(null));
    }
}