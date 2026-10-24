package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.vehicle.EmergencyVehicle;
import com.traffic.traffic.road.Lane;
public class EmergencyArrivalEvent extends SimulationEvent {
    private final EmergencyVehicle vehicle;
    private final String laneId;

    public EmergencyArrivalEvent(EmergencyVehicle vehicle, String laneId, double eventTime, int eventPriority) {
        super(eventTime, "Emergency_Arrival", eventPriority);
        if(vehicle == null){
            throw new IllegalArgumentException("EmergencyVehicle cannot be null");
        }
        if(laneId == null || laneId.trim().isEmpty()){
            throw new IllegalArgumentException("Lane ID cannot be null or empty");
        }
        this.vehicle = vehicle;
        this.laneId = laneId;
    }

    public String getEmergencyId() {
        return vehicle.getId();
    }

    @Override
    public void executeEvent(SimulationContext context) {
        if(context == null){
            throw new IllegalArgumentException("SimulationContext cannot be null");
        }

        Lane lane = context.getLane(laneId);
        lane.addVehicle(vehicle);

        System.out.println("Emergency Vehicle " + vehicle.getId() + " arrived in lane " + laneId + " at simulation time " + getEventTime());
    }
    
}
