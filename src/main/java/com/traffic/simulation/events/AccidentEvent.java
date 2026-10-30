
package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.road.Lane;

public class AccidentEvent extends SimulationEvent {

    private final String roadId;
    private final String laneId;

    // Backward-compatible constructor.
    public AccidentEvent(
            String laneId,
            double eventTime,
            int eventPriority) {

        this(null, laneId, eventTime, eventPriority);
    }

    // Preferred constructor.
    public AccidentEvent(
            String roadId,
            String laneId,
            double eventTime,
            int eventPriority) {

        super(eventTime, "Accident", eventPriority);

        if (roadId != null && roadId.isBlank()) {
            throw new IllegalArgumentException(
                "Road ID cannot be empty"
            );
        }

        if (laneId == null || laneId.isBlank()) {
            throw new IllegalArgumentException(
                "Lane ID cannot be empty"
            );
        }

        this.roadId = roadId;
        this.laneId = laneId;
    }

    public String getRoadId() {
        return roadId;
    }

    public String getLaneId() {
        return laneId;
    }

    @Override
    public void executeEvent(SimulationContext context) {

        if (context == null) {
            throw new IllegalArgumentException(
                "SimulationContext cannot be null"
            );
        }

        Lane lane = roadId == null
            ? context.getLane(laneId)
            : context.getLane(roadId, laneId);

        lane.blockLane();

        System.out.println(
            "Accident occurred on lane " + laneId
            + (roadId == null ? "" : " on road " + roadId)
            + " at simulation time " + getEventTime()
        );
    }
}
