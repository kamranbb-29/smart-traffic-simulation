package com.traffic.simulation.engine;

import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.simulation.events.SimulationEvent;

public class SimulationEngine {
    private final SimulationContext context;
    private final EventScheduler scheduler;
    private boolean isRunning;

    public SimulationEngine() {
        this.context= new SimulationContext(new SimulationClock());
        this.scheduler = new EventScheduler();
        this.isRunning = false;
    }

    public void run(){
        isRunning = true;

        while(isRunning && scheduler.hasEvents()){
            SimulationEvent Event = scheduler.getNextEvent();
            context.getClock().advanceTime(Event.getEventTime());
            Event.executeEvent();
        }

        isRunning = false;
    }

    public void stop(){
        isRunning = false;
    }

    public void reset(){
        context.getClock().reset();
        scheduler.clearEvents();
        isRunning = false;
    }

    public SimulationClock getClock() {
        return context.getClock();
    }

    public EventScheduler getScheduler() {
        return scheduler;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
