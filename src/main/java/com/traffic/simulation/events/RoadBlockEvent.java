package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.road.*;
public class RoadBlockEvent extends SimulationEvent{
    private final String roadId;

    public RoadBlockEvent(String roadId, double eventTime, int eventPriority){
        super(eventTime, "Road_Block", eventPriority);
        if(roadId == null || roadId.trim().isEmpty()){
            throw new IllegalArgumentException("Road ID cannot be null or empty");
        }
        this.roadId = roadId;
    }

    public String getRoadId() {
        return roadId;
    }

    @Override
    public void executeEvent(SimulationContext context) {
        if(context == null){
            throw new IllegalArgumentException("SimulationContext cannot be Null");
        }

        Road road = context.getRoad(roadId);
        road.blockRoad();
        for(Lane lane : road.getLanes()){
            lane.setRoadblock(true);
        }

        System.out.println("Road " + roadId + " is block at simulated time " + getEventTime());
    }
}
