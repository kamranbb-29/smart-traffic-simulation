package com.traffic.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

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

}
