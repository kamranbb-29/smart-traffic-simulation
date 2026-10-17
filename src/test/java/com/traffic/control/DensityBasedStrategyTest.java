package com.traffic.control;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

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

}
