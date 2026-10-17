package com.traffic.control;

public interface SignalControlStrategy {
    Direction chooseDirection(TrafficSnapshot snapshot);

}
