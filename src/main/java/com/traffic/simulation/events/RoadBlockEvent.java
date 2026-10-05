package com.traffic.simulation.events;

public class RoadBlockEvent extends SimulationEvent{
    private final String roadId;

    public RoadBlockEvent(String roadId, double eventTime, int eventPriority){
        super(eventTime, "Road_Block", eventPriority);
        this.roadId = roadId;
    }

    public String getRoadId() {
        return roadId;
    }

    @Override
    public void executeEvent() {
        System.out.println("Road " + roadId + "is blocked at simulated time " + getEventTime());
    }
}
