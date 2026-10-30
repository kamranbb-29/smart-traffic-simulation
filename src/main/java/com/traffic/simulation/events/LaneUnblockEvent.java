
package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.road.Lane;

public class LaneUnblockEvent extends SimulationEvent {

    private final String roadId;
    private final String laneId;

    public LaneUnblockEvent(
            String roadId,
            String laneId,
            double eventTime,
            int eventPriority) {

        super(eventTime, "Lane_Unblock", eventPriority);

        if (roadId == null || roadId.isBlank()) {
            throw new IllegalArgumentException(
                "Road ID cannot be null or empty"
            );
        }

        if (laneId == null || laneId.isBlank()) {
            throw new IllegalArgumentException(
                "Lane ID cannot be null or empty"
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

        Lane lane = context.getLane(roadId, laneId);

        lane.unblockLane();

        System.out.println(
            "Lane " + laneId + " on road " + roadId
            + " unblocked at simulation time " + getEventTime()
        );

        // Scheduling coordinator checks signal and road conditions.
        context.scheduleMovementForLane(
            roadId,
            laneId,
            getEventTime(),
            getEventPriority()
        );
    }
}
