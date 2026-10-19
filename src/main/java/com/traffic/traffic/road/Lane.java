
package com.traffic.traffic.road;

import com.traffic.traffic.vehicle.Vehicle;
import java.util.ArrayDeque;
import java.util.Queue;



public class Lane {

    private final String id;
    private final Queue<Vehicle> vehicles;

    public Lane(String id) {
    if (id == null || id.isBlank()) {
        throw new IllegalArgumentException(
            "Lane ID cannot be empty"
        );
    }

    this.id = id;
    this.vehicles = new ArrayDeque<>();
    }

    public String getId() {
    return id;
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException(
                "Vehicle cannot be null"
            );
        }

    vehicles.offer(vehicle);
    }

    public Vehicle getNextVehicle() {
    return vehicles.peek();
    }

    public Vehicle removeNextVehicle() {
    return vehicles.poll();
    }
    
    public int getVehicleCount() {  
    return vehicles.size();
    }

    public boolean isEmpty() {
    return vehicles.isEmpty();
    }
    
}