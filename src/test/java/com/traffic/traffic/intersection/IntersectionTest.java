package com.traffic.traffic.intersection;

import com.traffic.traffic.road.Road;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.vehicle.Car;

import org.junit.jupiter.api.Test;
import java.util.List;

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
}