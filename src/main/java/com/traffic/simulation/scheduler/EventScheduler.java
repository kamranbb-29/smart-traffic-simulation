package com.traffic.simulation.scheduler;
import com.traffic.simulation.events.SimulationEvent;

import java.util.PriorityQueue;

public class EventScheduler {
    private final PriorityQueue<SimulationEvent> eventQueue;

    public EventScheduler(){
        this.eventQueue = new PriorityQueue<>(
            (event1, event2) -> {
                int timeComparison = Double.compare(event1.getEventTime(), event2.getEventTime());
                if(timeComparison != 0) {
                    return timeComparison;
                }
                return Integer.compare(event1.getEventPriority(), event2.getEventPriority());
            }
        );
    }

    public void scheduleEvent(SimulationEvent event){
        eventQueue.add(event);
    }

    public SimulationEvent getNextEvent(){
        return eventQueue.poll();
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
