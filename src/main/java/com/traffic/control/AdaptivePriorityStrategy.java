package com.traffic.control;

import java.util.Map;

public class AdaptivePriorityStrategy implements SignalControlStrategy {

    @Override
    public Direction chooseDirection(TrafficSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Traffic snapshot cannot be null");
        }
        if (snapshot.getTrafficData() == null || snapshot.getTrafficData().isEmpty()) {
            return null;
        }

        Direction direc = null;
        int count = 0;
        double waitingTime = 0.0;

        for (Map.Entry<Direction, DirectionTraffic> entry : snapshot.getTrafficData().entrySet()) {

            Direction direction = entry.getKey();
            DirectionTraffic traffic = entry.getValue();

            if (traffic.getEmergencyVehicleCount() > 0) {

                if (traffic.getEmergencyVehicleCount() > count) {
                    count = traffic.getEmergencyVehicleCount();
                    direc = direction;
                    waitingTime = traffic.getWaitingTime();

                } else if (traffic.getEmergencyVehicleCount() == count) {
                    if (traffic.getWaitingTime() > waitingTime) {
                        direc = direction;
                        waitingTime = traffic.getWaitingTime();

                    } else if (traffic.getWaitingTime() == waitingTime) {
                        if (direction.ordinal() < direc.ordinal()) {
                            direc = direction;
                            waitingTime = traffic.getWaitingTime();

                        }
                    }
                }

            }

        }

        if (direc != null) {
            return direc;
        }
        Direction tempDirec = null;
        double tempWaitingTime = -1.0;
        for (Map.Entry<Direction, DirectionTraffic> entry : snapshot.getTrafficData().entrySet()) {

            Direction direction = entry.getKey();
            DirectionTraffic traffic = entry.getValue();

            if (traffic.getMaxWaitingTime() >= 120.0) {
                if (traffic.getMaxWaitingTime() > tempWaitingTime) {
                    tempDirec = direction;
                    tempWaitingTime = traffic.getMaxWaitingTime();
                } else if (traffic.getMaxWaitingTime() == tempWaitingTime) {
                    if (direction.ordinal() < tempDirec.ordinal()) {
                        tempDirec = direction;

                    }
                }
            }

        }

        if (tempDirec != null) {
            return tempDirec;
        }

        PriorityCalculator PC = new PriorityCalculator();

        double priorityScore = -1;

        for (Map.Entry<Direction, DirectionTraffic> entry : snapshot.getTrafficData().entrySet()) {

            Direction direction = entry.getKey();
            DirectionTraffic traffic = entry.getValue();

            double currPriorityScore = PC.calculatePriorityScore(traffic);

            if (currPriorityScore > priorityScore) {
                direc = direction;
                priorityScore = currPriorityScore;
            } else if (currPriorityScore == priorityScore) {
                if (direction.ordinal() < direc.ordinal()) {
                    direc = direction;
                }
            }

        }

        return direc;

    }

}
