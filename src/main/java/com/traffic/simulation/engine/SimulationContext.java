package com.traffic.simulation.engine;

import com.traffic.traffic.road.Lane;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Road;
public class SimulationContext {
    private final SimulationClock clock;
    private final Intersection intersection;

    public SimulationContext(SimulationClock clock, Intersection intersection) {
        if(clock == null){
            throw new IllegalArgumentException("SimulationClock cannot be null");
        }
        if(intersection == null){
            throw new IllegalArgumentException("Intersection cannot be null");
        }
        this.clock = clock;
        this.intersection = intersection;
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
}
