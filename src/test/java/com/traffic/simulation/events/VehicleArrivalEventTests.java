package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.traffic.vehicle.Car;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleArrivalEventTests {
    @Test 
    void vehicleArrivalAddsVehicleToLane() {
        SimulationEngine engine = new SimulationEngine();
        Lane lane = new Lane("L1");
        engine.getContext().addLane(lane);
        Vehicle vehicle = new Car("C1", 10.0);
        VehicleArrivalEvent event = new VehicleArrivalEvent(vehicle, lane.getId(), vehicle.getArrivalTime(), 1);

        engine.getScheduler().scheduleEvent(event);
        engine.run();

        assertEquals(1, lane.getVehicleCount());
        assertSame(vehicle, lane.getNextVehicle());
    }
}
