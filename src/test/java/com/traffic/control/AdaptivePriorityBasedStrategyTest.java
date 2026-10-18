package com.traffic.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class AdaptivePriorityBasedStrategyTest {

    @Test
    void AdaptiveTest1() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(10, 0, 2));
        trafficData.put(Direction.SOUTH, new DirectionTraffic(4, 0, 10));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        AdaptivePriorityStrategy aps = new AdaptivePriorityStrategy();

        assertEquals(Direction.SOUTH, aps.chooseDirection(snapshot));

    }

    @Test
    void AdaptiveTest2() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(10, 1, 2));
        trafficData.put(Direction.EAST, new DirectionTraffic(4, 2, 10));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        AdaptivePriorityStrategy aps = new AdaptivePriorityStrategy();

        assertEquals(Direction.EAST, aps.chooseDirection(snapshot));

    }

    @Test
    void AdaptiveTest3() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(0, 0, 0));
        trafficData.put(Direction.EAST, new DirectionTraffic(0, 0, 0));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        AdaptivePriorityStrategy aps = new AdaptivePriorityStrategy();

        assertEquals(Direction.NORTH, aps.chooseDirection(snapshot));

    }

    @Test
    void AdaptiveTest4() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(3, 2, 300));
        trafficData.put(Direction.EAST, new DirectionTraffic(8, 1, 1500));
        trafficData.put(Direction.SOUTH, new DirectionTraffic(8, 2, 900));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        AdaptivePriorityStrategy aps = new AdaptivePriorityStrategy();

        assertEquals(Direction.SOUTH, aps.chooseDirection(snapshot));

    }

    @Test
    void AdaptiveTest5() {
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(3, 2, 900));
        trafficData.put(Direction.EAST, new DirectionTraffic(8, 2, 900));
        trafficData.put(Direction.SOUTH, new DirectionTraffic(8, 2, 900));

        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        AdaptivePriorityStrategy aps = new AdaptivePriorityStrategy();

        assertEquals(Direction.NORTH, aps.chooseDirection(snapshot));

    }

}
