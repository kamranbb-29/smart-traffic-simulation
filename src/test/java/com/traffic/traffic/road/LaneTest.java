
package com.traffic.traffic.road;

import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.Bike;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LaneTest {

    @Test
    void shouldAddAndRemoveVehiclesInOrder() {
        Lane lane = new Lane("L1");

        Car car = new Car("C1", 0);
        Bike bike = new Bike("B1", 1);

        lane.addVehicle(car);
        lane.addVehicle(bike);

        assertEquals(2, lane.getVehicleCount());
        assertEquals("C1", lane.getNextVehicle().getId());

        assertSame(car, lane.removeNextVehicle());
        assertEquals("B1", lane.getNextVehicle().getId());
        assertEquals(1, lane.getVehicleCount());
    }

    @Test
    void shouldHandleEmptyLane() {
        Lane lane = new Lane("L2");

        assertTrue(lane.isEmpty());
        assertNull(lane.getNextVehicle());
        assertNull(lane.removeNextVehicle());
        assertEquals(0, lane.getVehicleCount());
    }

    @Test
    void shouldRejectNullVehicle() {
        Lane lane = new Lane("L3");

        assertThrows(
            IllegalArgumentException.class,
            () -> lane.addVehicle(null)
        );
    }

    @Test
    void shouldRejectInvalidLaneId() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Lane(null)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new Lane(" ")
        );
    }

    // New tests for lane blocking

    @Test
    void shouldBeUnblockedByDefault() {
        Lane lane = new Lane("L4");

        assertFalse(lane.isBlocked());
    }

    @Test
    void shouldBlockLaneAfterAccident() {
        Lane lane = new Lane("L5");

        lane.blockLane();

        assertTrue(lane.isBlocked());
    }

    @Test
    void shouldUnblockLaneAfterAccidentIsCleared() {
        Lane lane = new Lane("L6");

        lane.blockLane();
        assertTrue(lane.isBlocked());

        lane.unblockLane();

        assertFalse(lane.isBlocked());
    }
}