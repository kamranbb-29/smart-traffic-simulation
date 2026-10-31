package com.traffic.control;

import java.util.Map;

public class AdaptivePriorityStrategy implements SignalControlStrategy {

    public Direction chooseDirection(TrafficSnapshot snapshot) {
        Direction direc = null;
        int count = 0;
        int waitingTime = 0;

        for (Map.Entry<Direction, DirectionTraffic> entry : snapshot.getTrafficData().entrySet()) {

            Direction direction = entry.getKey();
            DirectionTraffic traffic = entry.getValue();

            if (traffic.getEmergencyVehicleCount() > 0) {

                if (traffic.getEmergencyVehicleCount() > count) {
                    count = traffic.getEmergencyVehicleCount();
                    direc = direction;
                    waitingTime = (int) traffic.getWaitingTime();

                } else if (traffic.getEmergencyVehicleCount() == count) {
                    if (traffic.getWaitingTime() > waitingTime) {
                        direc = direction;
                        waitingTime = (int) traffic.getWaitingTime();

                    } else if (traffic.getWaitingTime() == waitingTime) {
                        if (direction.ordinal() < direc.ordinal()) {
                            direc = direction;
                            waitingTime = (int) traffic.getWaitingTime();

                        }
                    }
                }

            }

        }

        if (direc != null) {
            return direc;
        }
        Direction tempDirec = null;
        int tempWaitingTime = -1;
        for (Map.Entry<Direction, DirectionTraffic> entry : snapshot.getTrafficData().entrySet()) {

            Direction direction = entry.getKey();
            DirectionTraffic traffic = entry.getValue();

            if (traffic.getWaitingTime() >= 120) {
                if (traffic.getWaitingTime() > tempWaitingTime) {
                    tempDirec = direction;
                    tempWaitingTime = (int) traffic.getWaitingTime();
                } else if (traffic.getWaitingTime() == tempWaitingTime) {
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
