package com.traffic.control;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DensityBasedStrategyTest {

    @Test
    void DensityTest() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(5, 1, 1000));
        trafficData.put(Direction.SOUTH, new DirectionTraffic(3, 0, 500));
        trafficData.put(Direction.EAST, new DirectionTraffic(2, 0, 500));
        trafficData.put(Direction.WEST, new DirectionTraffic(1, 0, 500));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        DensityBasedStrategy dbs = new DensityBasedStrategy();

        assertEquals(Direction.NORTH, dbs.chooseDirection(snapshot));
    }

    @Test
    @DisplayName("Validation: null snapshot must throw IllegalArgumentException")
    void testNullSnapshotThrowsIllegalArgumentException() {
        DensityBasedStrategy strategy = new DensityBasedStrategy();
        assertThrows(IllegalArgumentException.class, () -> strategy.chooseDirection(null));
    }

    @Test
    @DisplayName("Idle: all-zero vehicle counts must return null")
    void testAllZeroVehicleCountsReturnsNull() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(0, 0, 0));
        trafficData.put(Direction.SOUTH, new DirectionTraffic(0, 0, 0));
        trafficData.put(Direction.EAST, new DirectionTraffic(0, 0, 0));
        trafficData.put(Direction.WEST, new DirectionTraffic(0, 0, 0));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        assertNull(strategy.chooseDirection(snapshot), "When all directions have 0 vehicles, strategy must return null");
    }

    @Test
    @DisplayName("Determinism: 4-way equal tie must break deterministically to NORTH (ordinal 0)")
    void testDeterministicTieBreakingAllEqualCounts() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        for (Direction d : Direction.values()) {
            trafficData.put(d, new DirectionTraffic(5, 0, 10.0));
        }

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        assertEquals(Direction.NORTH, strategy.chooseDirection(snapshot));
    }

    @Test
    @DisplayName("Determinism: subset tie breaks to lowest ordinal (EAST < WEST)")
    void testDeterministicTieBreakingSubset() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(2, 0, 5.0));
        trafficData.put(Direction.SOUTH, new DirectionTraffic(2, 0, 5.0));
        trafficData.put(Direction.EAST, new DirectionTraffic(10, 0, 10.0));
        trafficData.put(Direction.WEST, new DirectionTraffic(10, 0, 10.0));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        assertEquals(Direction.EAST, strategy.chooseDirection(snapshot), "EAST (ordinal 1) must win over WEST (ordinal 3)");
    }

    @Test
    @DisplayName("Determinism: map insertion order reversal does not change selected direction")
    void testMapInsertionOrderReversalIndependence() {
        DensityBasedStrategy strategy = new DensityBasedStrategy();

        // Insertion order WEST, SOUTH, EAST, NORTH
        Map<Direction, DirectionTraffic> reversedMap = new LinkedHashMap<>();
        reversedMap.put(Direction.WEST, new DirectionTraffic(7, 0, 0));
        reversedMap.put(Direction.SOUTH, new DirectionTraffic(7, 0, 0));
        reversedMap.put(Direction.EAST, new DirectionTraffic(7, 0, 0));
        reversedMap.put(Direction.NORTH, new DirectionTraffic(7, 0, 0));

        TrafficSnapshot snapshot = new TrafficSnapshot(reversedMap);
        assertEquals(Direction.NORTH, strategy.chooseDirection(snapshot),
                "Tie-breaking must strictly follow Direction.values() ordinal, independent of map insertion order");
    }
}
