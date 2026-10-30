
package com.traffic.traffic.road;

import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.simulation.events.MovementType;
import com.traffic.traffic.vehicle.Bike;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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

    @Test
    void shouldStoreConfiguredPermittedMovement() {
        Lane lane = new Lane("L7", MovementType.LEFT);

        assertEquals(MovementType.LEFT, lane.getPermittedMovement());
    }

    @Test
    void shouldPermitStraightAndConfiguredMovement() {
        Lane leftLane = new Lane("L8", MovementType.LEFT);
        Lane rightLane = new Lane("L9", MovementType.RIGHT);

        assertTrue(leftLane.isMovementPermitted(MovementType.LEFT));
        assertTrue(leftLane.isMovementPermitted(MovementType.STRAIGHT));

        assertTrue(rightLane.isMovementPermitted(MovementType.RIGHT));
        assertTrue(rightLane.isMovementPermitted(MovementType.STRAIGHT));
    }

    @Test
    void shouldRejectIncompatibleMovement() {
        Lane leftLane = new Lane("L10", MovementType.LEFT);
        Lane rightLane = new Lane("L11", MovementType.RIGHT);

        assertFalse(leftLane.isMovementPermitted(MovementType.RIGHT));
        assertFalse(rightLane.isMovementPermitted(MovementType.LEFT));
    }

    @Test
    void shouldRejectNullMovement() {
        Lane lane = new Lane("L12", MovementType.LEFT);

        assertThrows(
            IllegalArgumentException.class,
            () -> lane.isMovementPermitted(null)
        );
    }

    @Test
    void shouldUpdatePermittedMovement() {
        Lane lane = new Lane("L13", MovementType.LEFT);

        lane.setPermittedMovement(MovementType.RIGHT);

        assertEquals(MovementType.RIGHT, lane.getPermittedMovement());
        assertTrue(lane.isMovementPermitted(MovementType.RIGHT));
        assertFalse(lane.isMovementPermitted(MovementType.LEFT));
    }

    @Test
    void shouldRejectNullPermittedMovement() {
        Lane lane = new Lane("L14", MovementType.LEFT);

        assertThrows(
            IllegalArgumentException.class,
            () -> lane.setPermittedMovement(null)
        );

        // Original configuration should remain unchanged.
        assertEquals(MovementType.LEFT, lane.getPermittedMovement());
    }

    @Test
    void shouldReturnImmutableVehicleList() {
        Lane lane = new Lane("L15");
        lane.addVehicle(new Car("C15", 0));

        List<Vehicle> vehicles = lane.getVehicles();

        assertEquals(1, vehicles.size());

        assertThrows(
            UnsupportedOperationException.class,
            () -> vehicles.add(new Bike("B15", 1))
        );
    }

    @Test
    void shouldPreserveVehiclesWhenLaneIsBlocked() {
        Lane lane = new Lane("L16");
        Car car = new Car("C16", 0);

        lane.addVehicle(car);
        lane.blockLane();

        assertTrue(lane.isBlocked());
        assertEquals(1, lane.getVehicleCount());
        assertSame(car, lane.getNextVehicle());
    }

    @Test
    void shouldTrackRoadWideBlockage() {
        Lane lane = new Lane("L17");

        lane.setRoadblock(true);

        assertTrue(lane.isRoadWideBlocked());
        assertTrue(lane.isBlocked());
        assertFalse(lane.isIndividuallyBlocked());
    }

    @Test
    void shouldUnblockRoadWideBlockage() {
        Lane lane = new Lane("L18");

        lane.setRoadblock(true);
        lane.setRoadblock(false);

        assertFalse(lane.isRoadWideBlocked());
        assertFalse(lane.isBlocked());
    }

    @Test
    void shouldPreserveRoadBlockWhenIndividualBlockIsCleared() {
        Lane lane = new Lane("L19");

        lane.setRoadblock(true);
        lane.blockLane();

        lane.unblockLane();

        assertTrue(lane.isRoadWideBlocked());
        assertFalse(lane.isIndividuallyBlocked());
        assertTrue(lane.isBlocked());
    }

    @Test
    void shouldPreserveIndividualBlockWhenRoadIsUnblocked() {
        Lane lane = new Lane("L20");

        lane.blockLane();
        lane.setRoadblock(true);

        lane.setRoadblock(false);

        assertTrue(lane.isIndividuallyBlocked());
        assertFalse(lane.isRoadWideBlocked());
        assertTrue(lane.isBlocked());
    }

    @Test
    void shouldUnblockOnlyWhenBothBlockagesAreCleared() {
        Lane lane = new Lane("L21");

        lane.blockLane();
        lane.setRoadblock(true);

        lane.unblockLane();
        assertTrue(lane.isBlocked());

        lane.setRoadblock(false);
        assertFalse(lane.isBlocked());
    }
}