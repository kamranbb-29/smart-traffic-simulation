package com.traffic.simulation.events;

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
    public void executeEvent() {
        System.out.println("Road " + roadId + " is unblocked at simulated time " + getEventTime());
    }
}
