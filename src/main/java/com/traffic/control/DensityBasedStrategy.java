package com.traffic.control;

import java.util.Map;

public class DensityBasedStrategy implements SignalControlStrategy {

    @Override
    public Direction chooseDirection(TrafficSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Traffic snapshot cannot be null");
        }

        Map<Direction, DirectionTraffic> trafficData = snapshot.getTrafficData();
        if (trafficData == null || trafficData.isEmpty()) {
            return null;
        }

        Direction bestDirection = null;
        int maxVehicleCount = 0;

        for (Direction direction : Direction.values()) {
            DirectionTraffic traffic = trafficData.get(direction);
            if (traffic != null && traffic.getVehicleCount() > maxVehicleCount) {
                maxVehicleCount = traffic.getVehicleCount();
                bestDirection = direction;
            }
        }

        return bestDirection;
    }

}
