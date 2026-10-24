package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.vehicle.EmergencyVehicle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class EmergencyArrivalEventTests{
    @Test
    void emergencyArrivalAddsVehicleToLane(){
        SimulationEngine engine = new SimulationEngine();
        Lane lane = new Lane("L1");
        engine.getContext().addLane(lane);
        EmergencyVehicle emergencyVehicle = new EmergencyVehicle("E1", 10.0);
        EmergencyArrivalEvent event = new EmergencyArrivalEvent(emergencyVehicle, lane.getId(), emergencyVehicle.getArrivalTime(), 1);

        engine.getScheduler().scheduleEvent(event);
        engine.run();

        assertEquals(1, lane.getVehicleCount());
        assertSame(emergencyVehicle, lane.getNextVehicle());
    }
}
