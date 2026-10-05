package com.traffic.simulation.engine;

public class SimulationContext {
    private final SimulationClock clock;

    public SimulationContext(SimulationClock clock){
        this.clock = clock;
    }

    public SimulationClock getClock() {
        return clock;
    }
}
