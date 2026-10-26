package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.vehicle.EmergencyVehicle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmergencyArrivalEventTests {

    @Test
    void emergencyArrivalAddsVehicleToLane() {
        SimulationEngine engine = new SimulationEngine();

        Intersection intersection = engine.getContext().getIntersection();

        Road road = new Road("R1");
        Lane lane = new Lane("L1");

        road.addLane(lane);
        intersection.addRoad(road);

        EmergencyVehicle emergencyVehicle = new EmergencyVehicle("E1", 10.0);

        EmergencyArrivalEvent event = new EmergencyArrivalEvent(
            emergencyVehicle,
            lane.getId(),
            emergencyVehicle.getArrivalTime(),
            1
        );

        engine.getScheduler().scheduleEvent(event);
        engine.run();

        assertEquals(1, lane.getVehicleCount());
        assertSame(emergencyVehicle, lane.getNextVehicle());
    }
}