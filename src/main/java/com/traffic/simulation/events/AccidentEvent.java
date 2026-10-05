package com.traffic.simulation.events;

public class AccidentEvent extends SimulationEvent{
    private final String roadId;

    public AccidentEvent(String roadId, double eventTime, int eventPriority){
        super(eventTime, "Accident", eventPriority);
        this.roadId = roadId;
    }

    public String getRoadId() {
        return roadId;
    }

    @Override
    public void executeEvent() {
        System.out.println("Accident occurred on road " + roadId + " at simulation time " + getEventTime());
    }
}
