package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
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
    public void executeEvent(SimulationContext context) {
        System.out.println("Accident occurred on road " + roadId + " at simulation time " + getEventTime());
    }
}
