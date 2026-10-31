package com.traffic.control;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DirectionTrafficTest {

    @Test
    void testDirectionTraffic() {
        DirectionTraffic directionTraffic = new DirectionTraffic(0, 0, 0);

        assertEquals(0, directionTraffic.getVehicleCount());
        assertEquals(0, directionTraffic.getEmergencyVehicleCount());
        assertEquals(0, directionTraffic.getWaitingTime());

    }

    @Test
    void testDirectionTrafficWithPositiveValues() {
        DirectionTraffic directionTraffic = new DirectionTraffic(5, 2, 1000);

        assertEquals(5, directionTraffic.getVehicleCount());
        assertEquals(2, directionTraffic.getEmergencyVehicleCount());
        assertEquals(1000, directionTraffic.getWaitingTime());

    }

    @Test
    void testDirectionTrafficWithNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(0, 0, -1));
    }

    @Test
    void testDirectionTrafficWithEmergencyVehicleCountGreaterThanVehicleCount() {
        assertThrows(IllegalArgumentException.class, () -> new DirectionTraffic(1, 2, 1000));

    }
}
