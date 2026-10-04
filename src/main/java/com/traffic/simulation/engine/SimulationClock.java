package com.traffic.simulation.engine;
public class SimulationClock {
    private double currentTime;
    
    public SimulationClock() {
        this.currentTime = 0.0;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void advvanceTime(double time) throws IllegalArgumentException {
        if(time < currentTime){
            throw new IllegalArgumentException("Time cannot go backwards.");
        }
        this.currentTime = time;
    }

    public void reset(){
        this.currentTime = 0.0;
    }
}
