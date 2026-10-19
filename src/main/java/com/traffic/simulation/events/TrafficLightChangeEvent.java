package com.traffic.simulation.events;

public class TrafficLightChangeEvent extends SimulationEvent{
    private final String signalId;
    private final String newState;

    public TrafficLightChangeEvent(String signalId, String newState, double eventTime, int eventPriority){
        super(eventTime, "Traffic_Light_Change", eventPriority);
        this.signalId = signalId;
        this.newState = newState;
    }

    public String getSignalId(){
        return this.signalId;
    }


    public String getNewState(){
        return this.newState;
    }

    @Override
    public void executeEvent() {
        System.out.println("Signal " + signalId + " changed to " + newState + " at simulation time " + getEventTime());
    }
}
