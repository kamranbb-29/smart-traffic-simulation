package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
public class EmergencyArrivalEvent extends SimulationEvent {
    private final String emergencyId;

    public EmergencyArrivalEvent(String emergencyId, double eventTime, int eventPriority) {
        super(eventTime, "Emergency_Arrival", eventPriority);
        this.emergencyId = emergencyId;
    }

    public String getEmergencyId() {
        return emergencyId;
    }

    @Override
    public void executeEvent(SimulationContext context) {
        System.out.println("Emergency " + emergencyId + " has arrived at time " + getEventTime());
    }
    
}
