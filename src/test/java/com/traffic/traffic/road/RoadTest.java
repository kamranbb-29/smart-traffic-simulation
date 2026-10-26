package com.traffic.traffic.road;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.Bike;
class RoadTest {

    @Test
    void shouldCreateRoadWithNoLanes() {
        Road road = new Road("R1");

        assertEquals("R1", road.getId());
        assertEquals(0, road.getLaneCount());
        assertEquals(0, road.getVehicleCount());
        assertTrue(road.isEmpty());
    }
    @Test
void shouldAddLanesAndCountVehicles() {
    Road road = new Road("R1");

    Lane lane1 = new Lane("L1");
    Lane lane2 = new Lane("L2");

    lane1.addVehicle(new Car("C1", 0));
    lane1.addVehicle(new Bike("B1", 1));
    lane2.addVehicle(new Car("C2", 2));

    road.addLane(lane1);
    road.addLane(lane2);

    assertEquals(2, road.getLaneCount());
    assertEquals(3, road.getVehicleCount());
    assertFalse(road.isEmpty());
}
@Test
void shouldRejectNullLane() {
    Road road = new Road("R1");

    assertThrows(
        IllegalArgumentException.class,
        () -> road.addLane(null)
    );
}

@Test
void shouldRejectDuplicateLaneIds() {
    Road road = new Road("R1");
    road.addLane(new Lane("L1"));

    assertThrows(
        IllegalArgumentException.class,
        () -> road.addLane(new Lane("L1"))
    );
}
@Test
void shouldReturnUnmodifiableLaneList() {
    Road road = new Road("R1");
    road.addLane(new Lane("L1"));

    List<Lane> lanes = road.getLanes();

    assertEquals(1, lanes.size());

    assertThrows(
        UnsupportedOperationException.class,
        () -> lanes.add(new Lane("L2"))
    );

    assertEquals(1, road.getLaneCount());
}
@Test
void shouldRejectInvalidRoadIds() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Road(null)
    );

    assertThrows(
        IllegalArgumentException.class,
        () -> new Road(" ")
    );
}
@Test
void shouldBeUnblockedByDefault() {
    Road road = new Road("R1");

    assertFalse(road.isBlocked());
}

@Test
void shouldBlockRoad() {
    Road road = new Road("R2");

    road.blockRoad();

    assertTrue(road.isBlocked());
}

@Test
void shouldUnblockRoad() {
    Road road = new Road("R3");

    road.blockRoad();
    road.unblockRoad();

    assertFalse(road.isBlocked());
}
}