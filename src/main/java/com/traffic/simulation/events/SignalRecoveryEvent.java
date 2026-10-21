package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
public class SignalRecoveryEvent extends SimulationEvent{
    private final String signalId;

    public SignalRecoveryEvent(String signalId, double eventTime, int eventPriority){
        super(eventTime, "Signal_Recovery", eventPriority);
        this.signalId = signalId;
    }

    public String getSignalId(){
        return this.signalId;
    }

    @Override
    public void executeEvent(SimulationContext context) {
        System.out.println("The Signal " + signalId + " is now operational at simulation time " + getEventTime());
    }
}
