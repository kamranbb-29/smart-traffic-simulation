package com.traffic.control;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TrafficSnapshotTest {

    @Test
    void testTrafficSnapshotCreation() {
        // Create a sample traffic data map
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.NORTH, new DirectionTraffic(5, 1, 1000));
        trafficData.put(Direction.SOUTH, new DirectionTraffic(3, 0, 500));

        // Create a TrafficSnapshot instance
        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);

        // Verify that the traffic data is correctly stored in the snapshot
        assertEquals(trafficData, snapshot.getTrafficData());
    }

    @Test
    void testImmutableTrafficData() {
        // Create a sample traffic data map
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.EAST, new DirectionTraffic(2, 0, 300));
        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);
        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.getTrafficData().clear());
    }

    @Test
    void testDefensiveCopy() {
        // creating a map
        Map<Direction, DirectionTraffic> trafficData = new HashMap<>();
        trafficData.put(Direction.EAST, new DirectionTraffic(2, 0, 300));
        TrafficSnapshot snapshot = new TrafficSnapshot(trafficData);

        trafficData.put(Direction.NORTH, new DirectionTraffic(2, 0, 300));
        assertEquals(1, snapshot.getTrafficData().size());
        assertFalse(snapshot.getTrafficData().containsKey(Direction.NORTH));
    }
}
