package com.traffic.simulation.engine;

import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.simulation.events.SimulationEvent;

public class SimulationEngine {
    private final SimulationContext context;
    private final EventScheduler scheduler;
    private boolean isRunning;

    public SimulationEngine() {
        this(new Intersection("INT1"));
    }

    public SimulationEngine(Intersection intersection) {
        if (intersection == null) {
            throw new IllegalArgumentException("Intersection cannot be null");
        }

        this.context = new SimulationContext(new SimulationClock(), intersection);
        this.scheduler = new EventScheduler();
        this.isRunning = false;
    }

    public void run(){
        isRunning = true;

        while(isRunning && scheduler.hasEvents()){
            SimulationEvent event = scheduler.getNextEvent();
            context.getClock().advanceTime(event.getEventTime());
            event.executeEvent(context);
        }

        isRunning = false;
    }

    public void stop(){
        isRunning = false;
    }

    public void reset(){
        isRunning = false;
        context.getClock().reset();
        scheduler.clearEvents();
        
    }

    public SimulationContext getContext() {
        return context;
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
