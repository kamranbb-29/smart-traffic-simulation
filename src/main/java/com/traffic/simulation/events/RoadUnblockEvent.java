package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
public class RoadUnblockEvent extends SimulationEvent{
    private final String roadId;
    
    public RoadUnblockEvent(String roadId, double eventTime, int eventPriority){
        super(eventTime, "Road_Unblock", eventPriority);
        this.roadId = roadId;
    }

    public String getRoadId() {
        return roadId;
    }

    @Override
    public void executeEvent(SimulationContext context) {
        System.out.println("Road " + roadId + " is unblocked at simulated time " + getEventTime());
    }
}
