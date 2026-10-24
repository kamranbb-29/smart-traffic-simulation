
package com.traffic.traffic.intersection;

import com.traffic.control.Direction;
import com.traffic.control.DirectionTraffic;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.traffic.vehicle.EmergencyVehicle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Intersection {

    private final String id;
    private final List<Road> roads;

    public Intersection(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "Intersection ID cannot be empty"
            );
        }

        this.id = id;
        this.roads = new ArrayList<>();
    }

    public void addRoad(Road road) {
        if (road == null) {
            throw new IllegalArgumentException(
                "Road cannot be null"
            );
        }

        for (Road existingRoad : roads) {
            if (existingRoad.getId().equals(road.getId())) {
                throw new IllegalArgumentException(
                    "Road with this ID already exists"
                );
            }
        }

        roads.add(road);
    }

    public String getId() {
        return id;
    }

    public List<Road> getRoads() {
        return List.copyOf(roads);
    }

    public int getRoadCount() {
        return roads.size();
    }

    public int getVehicleCount() {
        int count = 0;

        for (Road road : roads) {
            count += road.getVehicleCount();
        }

        return count;
    }

    public boolean isEmpty() {
        return getVehicleCount() == 0;
    }

    public Map<Direction, DirectionTraffic> getCurrentTrafficState() {
        Map<Direction, Integer> vehicleCounts =
            new EnumMap<>(Direction.class);
        Map<Direction, Integer> emergencyCounts =
            new EnumMap<>(Direction.class);
        Map<Direction, Long> waitingTimes =
            new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            vehicleCounts.put(direction, 0);
            emergencyCounts.put(direction, 0);
            waitingTimes.put(direction, 0L);
        }

        for (Road road : roads) {
            Direction direction = road.getDirection();

            if (direction == null) {
                throw new IllegalStateException(
                    "Road " + road.getId() +
                    " has no direction assigned"
                );
            }

            for (Lane lane : road.getLanes()) {
                for (Vehicle vehicle : getVehiclesInLane(lane)) {
                    vehicleCounts.merge(direction, 1, Integer::sum);

                    if (vehicle instanceof EmergencyVehicle) {
                        emergencyCounts.merge(
                            direction, 1, Integer::sum
                        );
                    }

                    waitingTimes.merge(
                        direction,
                        (long) vehicle.getWaitingTime(),
                        Long::sum
                    );
                }
            }
        }

        Map<Direction, DirectionTraffic> result =
            new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            result.put(
                direction,
                new DirectionTraffic(
                    vehicleCounts.get(direction),
                    emergencyCounts.get(direction),
                    waitingTimes.get(direction)
                )
            );
        }

        return Map.copyOf(result);
    }

    private List<Vehicle> getVehiclesInLane(Lane lane) {
        return lane.getVehicles();
    }
}
