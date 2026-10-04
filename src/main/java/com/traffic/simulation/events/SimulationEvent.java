package com.traffic.simulation.events;

public abstract class SimulationEvent {
    private double eventTime;
    private String eventType;
    private int eventPriority;

    public SimulationEvent(double eventTime, String eventType, int eventPriority) {
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.eventPriority = eventPriority;
    }

    public int getEventPriority() {
        return eventPriority;
    }

    public double getEventTime() {
        return eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    abstract public void executeEvent();
}
