
package com.traffic.traffic.intersection;

import com.traffic.control.Direction;
import com.traffic.control.DirectionTraffic;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.EmergencyVehicle;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionTest {

    @Test
    void shouldCreateEmptyIntersection() {
        Intersection intersection = new Intersection("I1");

        assertEquals("I1", intersection.getId());
        assertEquals(0, intersection.getRoadCount());
        assertEquals(0, intersection.getVehicleCount());
        assertTrue(intersection.isEmpty());
    }

    @Test
    void shouldAddRoadsAndCountVehicles() {
        Intersection intersection = new Intersection("I1");

        Road road1 = new Road("R1");
        Road road2 = new Road("R2");

        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        lane1.addVehicle(new Car("C1", 0));
        lane2.addVehicle(new Car("C2", 1));

        road1.addLane(lane1);
        road2.addLane(lane2);

        intersection.addRoad(road1);
        intersection.addRoad(road2);

        assertEquals(2, intersection.getRoadCount());
        assertEquals(2, intersection.getVehicleCount());
        assertFalse(intersection.isEmpty());
    }

    @Test
    void shouldRejectNullRoad() {
        Intersection intersection = new Intersection("I1");

        assertThrows(
            IllegalArgumentException.class,
            () -> intersection.addRoad(null)
        );
    }

    @Test
    void shouldRejectDuplicateRoadIds() {
        Intersection intersection = new Intersection("I1");
        intersection.addRoad(new Road("R1"));

        assertThrows(
            IllegalArgumentException.class,
            () -> intersection.addRoad(new Road("R1"))
        );
    }

    @Test
    void shouldRejectInvalidIntersectionIds() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Intersection(null)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new Intersection(" ")
        );
    }

    @Test
    void shouldReturnUnmodifiableRoadList() {
        Intersection intersection = new Intersection("I1");
        intersection.addRoad(new Road("R1"));

        List<Road> roads = intersection.getRoads();

        assertEquals(1, roads.size());

        assertThrows(
            UnsupportedOperationException.class,
            () -> roads.add(new Road("R2"))
        );

        assertEquals(1, intersection.getRoadCount());
    }

    @Test
    void shouldReturnTrafficStateForAllDirections() {
        Intersection intersection = new Intersection("I1");

        Road northRoad = new Road("R1", Direction.NORTH);
        Lane northLane = new Lane("L1");

        northLane.addVehicle(new Car("C1", 0));
        northLane.addVehicle(new EmergencyVehicle("E1", 1));

        northRoad.addLane(northLane);
        intersection.addRoad(northRoad);

        Map<Direction, DirectionTraffic> traffic =
            intersection.getCurrentTrafficState();

        assertEquals(4, traffic.size());

        assertEquals(
            2,
            traffic.get(Direction.NORTH).getVehicleCount()
        );

        assertEquals(
            1,
            traffic.get(Direction.NORTH).getEmergencyVehicleCount()
        );

        assertEquals(0,
            traffic.get(Direction.EAST).getVehicleCount());

        assertEquals(0,
            traffic.get(Direction.SOUTH).getVehicleCount());

        assertEquals(0,
            traffic.get(Direction.WEST).getVehicleCount());
    }

    @Test
    void shouldAggregateVehiclesFromMultipleRoadsInSameDirection() {
        Intersection intersection = new Intersection("I1");

        Road road1 = new Road("R1", Direction.NORTH);
        Road road2 = new Road("R2", Direction.NORTH);

        Lane lane1 = new Lane("L1");
        Lane lane2 = new Lane("L2");

        lane1.addVehicle(new Car("C1", 0));
        lane2.addVehicle(new Car("C2", 1));

        road1.addLane(lane1);
        road2.addLane(lane2);

        intersection.addRoad(road1);
        intersection.addRoad(road2);

        Map<Direction, DirectionTraffic> traffic =
            intersection.getCurrentTrafficState();

        assertEquals(
            2,
            traffic.get(Direction.NORTH).getVehicleCount()
        );
    }

    @Test
    void shouldRejectRoadWithoutDirectionWhenGettingTrafficState() {
        Intersection intersection = new Intersection("I1");
        intersection.addRoad(new Road("R1"));

        assertThrows(
            IllegalStateException.class,
            intersection::getCurrentTrafficState
        );
    }
    @Test
void shouldAggregateWaitingTimeByDirection() {
    Intersection intersection = new Intersection("I1");

    Road road = new Road("R1", Direction.NORTH);
    Lane lane = new Lane("L1");

    Car car = new Car("C1", 0);
    car.addWaitingTime(10);

    lane.addVehicle(car);
    road.addLane(lane);
    intersection.addRoad(road);

    Map<Direction, DirectionTraffic> traffic =
        intersection.getCurrentTrafficState();

    assertEquals(
        10L,
        traffic.get(Direction.NORTH).getWaitingTime()
    );
}
}
