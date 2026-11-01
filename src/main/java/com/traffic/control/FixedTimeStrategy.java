package com.traffic.control;

public class FixedTimeStrategy implements SignalControlStrategy {
    private int index = 0;

    @Override
    public Direction chooseDirection(TrafficSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Traffic snapshot cannot be null");
        }

        Direction finalDirec = Direction.values()[index];
        index = (index + 1) % 4;

        return finalDirec;

    }

}
