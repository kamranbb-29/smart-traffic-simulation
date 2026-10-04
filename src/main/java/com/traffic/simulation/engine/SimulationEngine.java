package com.traffic.simulation.engine;

import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.simulation.events.SimulationEvent;

public class SimulationEngine {
    private final SimulationClock clock;
    private final EventScheduler scheduler;
    private boolean isRunning;

    public SimulationEngine() {
        this.clock = new SimulationClock();
        this.scheduler = new EventScheduler();
        this.isRunning = false;
    }

    public void run(){
        isRunning = true;

        while(isRunning && scheduler.hasEvents()){
            SimulationEvent Event = scheduler.getNextEvent();
            clock.advanceTime(Event.getEventTime());
            Event.executeEvent();
        }

        isRunning = false;
    }

    public void stop(){
        isRunning = false;
    }

    public void reset(){
        clock.reset();
        scheduler.clearEvents();
        isRunning = false;
    }

    public SimulationClock getClock() {
        return clock;
    }

    public EventScheduler getScheduler() {
        return scheduler;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
