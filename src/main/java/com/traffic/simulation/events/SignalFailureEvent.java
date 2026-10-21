package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
public class SignalFailureEvent extends SimulationEvent{
    private final String signalId;

    public SignalFailureEvent(String signalId, double eventTime, int eventPriority){
        super(eventTime, "Signal_Failure", eventPriority);
        this.signalId = signalId;
    }

    public String getSignalId(){
        return this.signalId;
    }

    @Override
    public void executeEvent(SimulationContext context) {
        System.out.println("The Signal " + signalId + " is not operational at simulation time " + getEventTime());
    }
}
