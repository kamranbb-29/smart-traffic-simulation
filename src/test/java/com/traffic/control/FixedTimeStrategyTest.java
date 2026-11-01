package com.traffic.control;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FixedTimeStrategyTest {

    @Test
    void FixedTest() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(5, 1, 1000));
        trafficData.put(Direction.SOUTH, new DirectionTraffic(3, 0, 500));
        trafficData.put(Direction.EAST, new DirectionTraffic(2, 0, 500));
        trafficData.put(Direction.WEST, new DirectionTraffic(1, 0, 500));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        FixedTimeStrategy fts = new FixedTimeStrategy();

        assertEquals(Direction.NORTH, fts.chooseDirection(snapshot));
    }

    @Test
    @DisplayName("Validation: null snapshot must throw IllegalArgumentException")
    void testNullSnapshotThrowsIllegalArgumentException() {
        FixedTimeStrategy fts = new FixedTimeStrategy();
        assertThrows(IllegalArgumentException.class, () -> fts.chooseDirection(null));
    }

    @Test
    @DisplayName("Cycling: fixed sequence NORTH -> EAST -> SOUTH -> WEST is preserved regardless of traffic presence")
    void testFixedTimeSequentialCycleMaintainedEvenWithoutVehicles() {
        Map<Direction, DirectionTraffic> emptyTraffic = new HashMap<>();
        for (Direction d : Direction.values()) {
            emptyTraffic.put(d, new DirectionTraffic(0, 0, 0));
        }

        TrafficSnapshot snapshot = new TrafficSnapshot(emptyTraffic);
        FixedTimeStrategy fts = new FixedTimeStrategy();

        assertEquals(Direction.NORTH, fts.chooseDirection(snapshot));
        assertEquals(Direction.EAST, fts.chooseDirection(snapshot));
        assertEquals(Direction.SOUTH, fts.chooseDirection(snapshot));
        assertEquals(Direction.WEST, fts.chooseDirection(snapshot));
        assertEquals(Direction.NORTH, fts.chooseDirection(snapshot));
    }
}
