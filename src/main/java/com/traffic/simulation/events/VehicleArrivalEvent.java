package com.traffic.simulation.events;

public class VehicleArrivalEvent extends SimulationEvent{
    private final String vehicleId;

    public VehicleArrivalEvent(String vehiclId, double eventTime, int eventPriority) {
        super(eventTime, "Vehicle_Arrival", eventPriority);
        this.vehicleId = vehiclId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public void executeEvent(){
        System.out.println("Vehicle " + vehicleId + " has arrived at time " + getEventTime());
    }
}
