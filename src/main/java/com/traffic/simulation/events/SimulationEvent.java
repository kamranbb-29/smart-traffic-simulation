package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
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

    abstract public void executeEvent(SimulationContext context);

    /**
     * Indicates whether this event introduces new vehicles (normal or emergency) into the simulation.
     * Future arrivals can add traffic and enable future progress.
     */
    public boolean isArrivalEvent() {
        return false;
    }

    /**
     * Indicates whether this event records the departure and completion of a vehicle.
     */
    public boolean isDepartureEvent() {
        return false;
    }

    /**
     * Indicates whether this event is a recovery action (e.g. unblocking a road/lane or recovering a signal)
     * that can resolve a blockage and enable progress for stuck vehicles.
     */
    public boolean isRecoveryEvent() {
        return false;
    }

    /**
     * Indicates whether this event has the capability to directly enable or produce vehicle progress
     * (e.g. arrivals, unblocking roads/lanes, recovering signals, vehicle movements, or green light transitions).
     * Recurring controller scheduling events return false because they only evaluate or cycle signals
     * without directly clearing physical blockages or introducing traffic.
     */
    public boolean canEnableProgress() {
        return isArrivalEvent() || isRecoveryEvent();
    }
}
