package com.traffic.traffic.road;

import java.util.ArrayList;
import java.util.List;

public class Road {

    private final String id;
    private final List<Lane> lanes;
    public Road(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "Road ID cannot be empty"
            );
        }

        this.id = id;
        this.lanes = new ArrayList<>();
    }
    public void addLane(Lane lane) {
        if (lane == null) {
            throw new IllegalArgumentException(
                "Lane cannot be null"
            );
        }
    

        for (Lane existingLane : lanes) {
            if (existingLane.getId().equals(lane.getId())) {
                throw new IllegalArgumentException(
                    "Lane with this ID already exists in the road"
                );
            }
        }

        lanes.add(lane);
    }
    public String getId() {
    return id;
    }

    public List<Lane> getLanes() {
        return List.copyOf(lanes);
    }
    public int getLaneCount() {
        return lanes.size();
    }

    public int getVehicleCount() {
        int count = 0;
        for (Lane lane : lanes) {
            count += lane.getVehicleCount();
        }

        return count;
    }
    
    public boolean isEmpty() {
        return getVehicleCount() == 0;
    }

}