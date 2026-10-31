package com.traffic.control;

import java.util.Map;
import java.util.HashMap;

import java.util.Collections;

public class TrafficSnapshot {
    private final Map<Direction, DirectionTraffic> trafficData;

    public TrafficSnapshot(Map<Direction, DirectionTraffic> trafficData) {
        if (trafficData == null || trafficData.isEmpty()) {
            throw new IllegalArgumentException("Traffic data cannot be null or empty");
        }
        this.trafficData = Collections.unmodifiableMap(new HashMap<>(trafficData));

    }

    public Map<Direction, DirectionTraffic> getTrafficData() {
        return trafficData;
    }

}
