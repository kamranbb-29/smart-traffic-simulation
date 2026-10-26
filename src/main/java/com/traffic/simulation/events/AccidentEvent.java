package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.road.Lane;

public class AccidentEvent extends SimulationEvent {

    private final String laneId;

    public AccidentEvent(String laneId, double eventTime, int eventPriority) {
        super(eventTime, "Accident", eventPriority);

        if (laneId == null || laneId.isBlank()) {
            throw new IllegalArgumentException("Lane ID cannot be empty");
        }

        this.laneId = laneId;
    }

    public String getLaneId() {
        return laneId;
    }

    @Override
    public void executeEvent(SimulationContext context) {
        if(context == null){
            throw new IllegalArgumentException("SimulationContext cannot be null");
        }
        Lane lane = context.getLane(laneId);
        lane.blockLane();

        System.out.println(
            "Accident occurred on lane " + laneId +
            " at simulation time " + getEventTime()
        );
    }
}