package com.traffic.control;

public class FixedTimeStrategy implements SignalControlStrategy {
    private int index = 0;

    public Direction chooseDirection(TrafficSnapshot snapshot) {

        Direction finalDirec = Direction.values()[index];
        index = (index + 1) % 4;

        return finalDirec;

    }

}
