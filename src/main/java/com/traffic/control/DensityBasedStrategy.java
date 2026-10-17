package com.traffic.control;

import java.util.Map;

public class DensityBasedStrategy implements SignalControlStrategy {

    public Direction chooseDirection(TrafficSnapshot snapshot) {

        int count = -1;
        Direction finalDirection = Direction.NORTH;

        for (Map.Entry<Direction, DirectionTraffic> entry : snapshot.getTrafficData().entrySet()) {

            Direction direction = entry.getKey();
            DirectionTraffic traffic = entry.getValue();

            if (traffic.getVehicleCount() > count) {
                count = traffic.getVehicleCount();
                finalDirection = direction;

            }

        }

        return finalDirection;

    }

}
