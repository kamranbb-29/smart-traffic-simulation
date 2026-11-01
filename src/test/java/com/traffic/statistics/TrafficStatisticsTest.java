
package com.traffic.statistics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrafficStatisticsTest {

    @Test
    public void testCompletedVehicles() {
        TrafficStatistics stats = new TrafficStatistics();

        stats.recordCompletedVehicle(10);
        stats.recordCompletedVehicle(20);
        stats.recordCompletedVehicle(30);

        assertEquals(3, stats.getCompletedVehicles());
    }

    @Test
    public void testAverageWaitingTime() {
        TrafficStatistics stats = new TrafficStatistics();

        stats.recordCompletedVehicle(10);
        stats.recordCompletedVehicle(20);
        stats.recordCompletedVehicle(30);

        assertEquals(20.0, stats.getAverageWaitingTime(), 0.001);
    }

    @Test
    public void testAverageWhenNoVehiclesCompleted() {
        TrafficStatistics stats = new TrafficStatistics();

        assertEquals(0.0, stats.getAverageWaitingTime(), 0.001);
    }

    @Test
    public void testNegativeWaitingTimeIsRejected() {
        TrafficStatistics stats = new TrafficStatistics();

        assertThrows(
            IllegalArgumentException.class,
            () -> stats.recordCompletedVehicle(-5)
        );
    }
}
