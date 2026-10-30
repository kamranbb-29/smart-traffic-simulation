
package com.traffic.traffic.road;

import com.traffic.control.Direction;
import com.traffic.simulation.events.MovementType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Road {

    private final String id;
    private final Direction direction;
    private final List<Lane> lanes;
    private boolean blocked = false;

    // Existing constructor — keeps old code compatible
    public Road(String id) {
        this(id, null);
    }

    // New constructor with direction
    public Road(String id, Direction direction) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "Road ID cannot be empty"
            );
        }

        this.id = id;
        this.direction = direction;
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

        // Apply the current road-wide blockage to newly added lanes
        lane.setRoadblock(blocked);
        lanes.add(lane);
    }

    public String getId() {
        return id;
    }

    public Direction getDirection() {
        return direction;
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

    // Block the entire road and all its lanes
    public void blockRoad() {
        this.blocked = true;

        for (Lane lane : lanes) {
            lane.setRoadblock(true);
        }
    }

    // Clear the road-wide blockage without clearing individual lane accidents
    public void unblockRoad() {
        this.blocked = false;

        for (Lane lane : lanes) {
            lane.setRoadblock(false);
        }
    }

    // Getter for road-wide blockage status
    public boolean isBlocked() {
        return blocked;
    }

    public Optional<Lane> findLeastLoadedCompatibleLane(
            MovementType movement) {

        if (movement == null) {
            throw new IllegalArgumentException(
                "Movement cannot be null"
            );
        }

        Lane bestLane = null;

        for (Lane lane : lanes) {

            if (!lane.isMovementPermitted(movement)) {
                continue;
            }

            if (bestLane == null ||
                    lane.getVehicleCount() < bestLane.getVehicleCount()) {
                bestLane = lane;
            }
        }

        return Optional.ofNullable(bestLane);
    }
}
