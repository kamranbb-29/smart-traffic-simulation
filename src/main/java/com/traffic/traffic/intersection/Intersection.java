package com.traffic.traffic.intersection;

import com.traffic.traffic.road.Road;

import java.util.ArrayList;
import java.util.List;

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
}

