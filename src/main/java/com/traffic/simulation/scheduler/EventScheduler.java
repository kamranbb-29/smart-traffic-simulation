package com.traffic.simulation.scheduler;
import com.traffic.simulation.events.SimulationEvent;

import java.util.PriorityQueue;

public class EventScheduler implements EventScheduling{
    private final PriorityQueue<ScheduledEvent> eventQueue;
    private long nextSequenceNumber = 0;

    public EventScheduler(){
        this.eventQueue = new PriorityQueue<>();
    }

    public void scheduleEvent(SimulationEvent event){
        ScheduledEvent scheduled = new ScheduledEvent(event, nextSequenceNumber++);
        eventQueue.add(scheduled);
    }

    public SimulationEvent getNextEvent(){
        ScheduledEvent scheduled = eventQueue.poll();
        
        if(scheduled == null){
            return null;
        }

        return scheduled.getEvent();
    }

    public boolean hasEvents(){
        return !eventQueue.isEmpty();
    }

    public int getEventCount() {
        return eventQueue.size();
    }

    public void clearEvents() {
        eventQueue.clear();
    }
}
