package com.traffic.simulation.engine;

import com.traffic.traffic.road.Lane;
import java.util.HashMap;
import java.util.Map;
public class SimulationContext {
    private final SimulationClock clock;
    private final Map<String, Lane> lanes;

    public SimulationContext(SimulationClock clock){
        this.clock = clock;
        this.lanes = new HashMap<>();
    }

    public SimulationClock getClock() {
        return clock;
    }

    public void addLane(Lane lane){
        if(lane == null){
            throw new IllegalArgumentException("Lane cannot be null");
        }

        if(lanes.containsKey(lane.getId())){
            throw new IllegalArgumentException("Lane with ID " + lane.getId() + " already exists");
        }

        lanes.put(lane.getId(), lane);
    }

    public Lane getLane(String laneId){
        Lane lane = lanes.get(laneId);

        if(lane == null){
            throw new IllegalArgumentException("Lane with ID " + laneId + " does not exist");
        }

        return lane;
    }
}
