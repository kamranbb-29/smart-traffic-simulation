
package com.traffic.traffic.road;

import java.util.List;
import com.traffic.traffic.vehicle.Vehicle;
import java.util.ArrayDeque;
import java.util.Queue;
import com.traffic.simulation.events.MovementType;

public class Lane {

    private final String id;
    private final Queue<Vehicle> vehicles;
    private MovementType permittedMovement;

    // Tracks blockage caused by an accident on this specific lane
    private boolean individuallyBlocked = false;

    // Tracks blockage caused by the entire road being blocked
    private boolean roadWideBlocked = false;

    public Lane(String id) {
        this(id, MovementType.STRAIGHT);
    }

    public Lane(String id, MovementType permittedMovement) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "Lane ID cannot be empty"
            );
        }
        if(permittedMovement == null){
            throw new IllegalArgumentException("Permitted Movement Cannot be null");
        }
        this.permittedMovement = permittedMovement;
        this.id = id;
        this.vehicles = new ArrayDeque<>();
    }

    public String getId() {
        return id;
    }

    public MovementType getPermittedMovement(){
        return permittedMovement;
    }

    public void setPermittedMovement(MovementType permittedMovement){
        if(permittedMovement == null){
            throw new IllegalArgumentException("Permitted Movement Cannot be null");
        }
        this.permittedMovement = permittedMovement;
    }

    public boolean isMovementPermitted(MovementType movement) {
        if (movement == null) {
            throw new IllegalArgumentException("Movement cannot be null");
        }

        return movement == MovementType.STRAIGHT || movement == this.permittedMovement;
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException(
                "Vehicle cannot be null"
            );
        }

        vehicles.add(vehicle);
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

    public List<Vehicle> getVehicles() {
        return List.copyOf(vehicles);
    }

    // Block this lane individually, e.g. due to an accident
    public void blockLane() {
        this.individuallyBlocked = true;
    }

    // Clear this lane's individual blockage
    public void unblockLane() {
        this.individuallyBlocked = false;
    }

    // Set or clear a road-wide blockage
    public void setRoadblock(boolean blocked) {
        this.roadWideBlocked = blocked;
    }

    // Returns true if either type of blockage is active
    public boolean isBlocked() {
        return individuallyBlocked || roadWideBlocked;
    }

    // Check individual blockage separately
    public boolean isIndividuallyBlocked() {
        return individuallyBlocked;
    }

    // Check road-wide blockage separately
    public boolean isRoadWideBlocked() {
        return roadWideBlocked;
    }
}
