package com.traffic.simulation.engine;

import com.traffic.traffic.road.Lane;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Road;
import com.traffic.control.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class SimulationContext {
    private final SimulationClock clock;
    private final Intersection intersection;
    private final Map<Direction, TrafficSignal> trafficSignals;
    private final Set<Direction> failedSignals;

    public SimulationContext(SimulationClock clock, Intersection intersection) {
        if(clock == null){
            throw new IllegalArgumentException("SimulationClock cannot be null");
        }
        if(intersection == null){
            throw new IllegalArgumentException("Intersection cannot be null");
        }
        this.clock = clock;
        this.intersection = intersection;
        this.trafficSignals = new HashMap<>();
        this.failedSignals = new HashSet<>();
    }

    public SimulationClock getClock() {
        return clock;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public Lane getLane(String laneId){
        if(laneId == null || laneId.trim().isEmpty()){
            throw new IllegalArgumentException("Lane ID cannot be null or empty");
        }
        if(intersection !=null){
            for(Road road : intersection.getRoads()){
                for(Lane lane : road.getLanes()){
                    if(lane.getId().equals(laneId)){
                        return lane;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Lane with id " + laneId + " does not exist");
    }

    public Road getRoad(String roadId){
        if(roadId == null || roadId.trim().isEmpty()){
            throw new IllegalArgumentException("Road id cannot be empty");
        }
        
        if(intersection != null){
            for(Road road : intersection.getRoads()){
                if(road.getId().equals(roadId)){
                    return road;
                }
            }
        }
        
        throw new IllegalArgumentException("Road with ID " + roadId + " does not exist");
    }

    public void addTrafficSignal(TrafficSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("TrafficSignal cannot be null");
        }

        Direction direction = signal.getDirection();

        if (direction == null) {
            throw new IllegalArgumentException("Signal direction cannot be null");
        }

        if (trafficSignals.containsKey(direction)) {
            throw new IllegalArgumentException(
                "Traffic signal already exists for " + direction
            );
        }

        trafficSignals.put(direction, signal);
    }

    public TrafficSignal getTrafficSignal(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }

        TrafficSignal signal = trafficSignals.get(direction);

        if (signal == null) {
            throw new IllegalArgumentException(
                "No traffic signal found for " + direction
            );
        }

        return signal;
    }

    public void failSignal(Direction direction) {
        getTrafficSignal(direction);
        failedSignals.add(direction);
    }

    public void recoverSignal(Direction direction) {
        getTrafficSignal(direction);
        failedSignals.remove(direction);
    }

    public boolean isSignalFailed(Direction direction) {
        getTrafficSignal(direction);
        return failedSignals.contains(direction);
    }
}
