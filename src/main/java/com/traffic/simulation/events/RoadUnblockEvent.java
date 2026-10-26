package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.road.*;
public class RoadUnblockEvent extends SimulationEvent{
    private final String roadId;
    
    public RoadUnblockEvent(String roadId, double eventTime, int eventPriority){
        super(eventTime, "Road_Unblock", eventPriority);
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
            throw new IllegalArgumentException("SimulationContext cannot be null");
        }

        Road road = context.getRoad(roadId);
        road.unblockRoad();
        for(Lane lane : road.getLanes()){
            lane.setRoadblock(false);
        }

        System.out.println("Road " + roadId + " is unblocked at simulated time " + getEventTime());
    }
}
