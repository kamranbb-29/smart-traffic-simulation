package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.traffic.road.Lane;
public class VehicleArrivalEvent extends SimulationEvent{
    private final Vehicle vehicle;
    private final String laneId;

    public VehicleArrivalEvent(Vehicle vehicle, String laneId, double eventTime, int eventPriority) {
        super(eventTime, "Vehicle_Arrival", eventPriority);
        if(vehicle == null){
            throw new IllegalArgumentException("Vehicle cannot be null or empty");
        }
        if(laneId == null || laneId.trim().isEmpty()){
            throw new IllegalArgumentException("Lane ID cannot be null or empty");
        }

        this.vehicle = vehicle;
        this.laneId = laneId;
    }

    public String getVehicleId() {
        return vehicle.getId();
    }

    @Override
    public void executeEvent(SimulationContext context){
        if(context == null){
            throw new IllegalArgumentException("SimulationContext cannot be null");
        }

        Lane lane = context.getLane(laneId);
        lane.addVehicle(vehicle);

        System.out.println("Vehicle " + vehicle.getId() + " arrived in lane " + laneId + " at simulation time " + getEventTime());
    }
}
