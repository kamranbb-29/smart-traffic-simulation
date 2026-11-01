package com.traffic.benchmark;

import com.traffic.control.Direction;
import com.traffic.simulation.events.MovementType;
import com.traffic.traffic.vehicle.Bike;
import com.traffic.traffic.vehicle.Bus;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.EmergencyVehicle;
import com.traffic.traffic.vehicle.Vehicle;

public class VehicleArrivalDescriptor {

    public enum VehicleType {
        CAR,
        BUS,
        BIKE,
        EMERGENCY
    }

    private final String id;
    private final VehicleType vehicleType;
    private final Direction direction;
    private final MovementType intendedMovement;
    private final double arrivalTime;

    public VehicleArrivalDescriptor(
            String id,
            VehicleType vehicleType,
            Direction direction,
            MovementType intendedMovement,
            double arrivalTime) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID cannot be empty");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("VehicleType cannot be null");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }
        if (intendedMovement == null) {
            throw new IllegalArgumentException("MovementType cannot be null");
        }
        if (arrivalTime < 0 || !Double.isFinite(arrivalTime)) {
            throw new IllegalArgumentException("Arrival time must be finite and non-negative");
        }

        this.id = id;
        this.vehicleType = vehicleType;
        this.direction = direction;
        this.intendedMovement = intendedMovement;
        this.arrivalTime = arrivalTime;
    }

    public String getId() {
        return id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Direction getDirection() {
        return direction;
    }

    public MovementType getIntendedMovement() {
        return intendedMovement;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public Vehicle instantiateVehicle() {
        return switch (vehicleType) {
            case CAR -> new Car(id, arrivalTime, intendedMovement);
            case BUS -> new Bus(id, arrivalTime, intendedMovement);
            case BIKE -> new Bike(id, arrivalTime, intendedMovement);
            case EMERGENCY -> new EmergencyVehicle(id, arrivalTime, intendedMovement);
        };
    }
}

