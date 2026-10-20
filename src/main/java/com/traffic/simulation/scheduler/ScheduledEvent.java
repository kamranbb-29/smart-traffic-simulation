package com.traffic.simulation.scheduler;

import com.traffic.simulation.events.SimulationEvent;
public class ScheduledEvent implements Comparable<ScheduledEvent> {
    private final SimulationEvent event;
    private final long sequenceNumber;

    public ScheduledEvent(SimulationEvent event, long sequenceNumber){
        this.event = event;
        this.sequenceNumber = sequenceNumber; 
    }

    @Override
    public int compareTo(ScheduledEvent other) {
        int timeComparison = Double.compare(event.getEventTime(), other.event.getEventTime());
        if (timeComparison != 0) {
            return timeComparison;
        }
        
        int priorityComparison = Integer.compare(event.getEventPriority(), other.event.getEventPriority());
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        return Long.compare(sequenceNumber, other.sequenceNumber);
    }

    public SimulationEvent getEvent() {
        return event;
    }

    
}
