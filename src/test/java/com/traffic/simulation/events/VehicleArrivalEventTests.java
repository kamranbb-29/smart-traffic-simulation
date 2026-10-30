
package com.traffic.simulation.events;

import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.Bike;
import com.traffic.control.Direction;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleArrivalEventTests {

    @Test
    void vehicleArrivalAddsVehicleToCompatibleLane() {
        SimulationEngine engine = new SimulationEngine();

        registerRedNorthSignal(engine);

        Intersection intersection = engine.getContext().getIntersection();

        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        road.addLane(lane);
        intersection.addRoad(road);

        Vehicle vehicle = new Car("C1", 10.0);

        VehicleArrivalEvent event = new VehicleArrivalEvent(
            vehicle, road.getId(), vehicle.getArrivalTime(), 1
        );

        engine.getScheduler().scheduleEvent(event);
        engine.run();

        assertEquals(1, lane.getVehicleCount());
        assertSame(vehicle, lane.getNextVehicle());
    }

    @Test
    void shouldAssignVehicleToLanePermittingItsIntendedMovement() {
        SimulationEngine engine = new SimulationEngine();
        registerRedNorthSignal(engine);

        Road road = new Road("R1", Direction.NORTH);

        Lane leftLane = new Lane("L1", MovementType.LEFT);
        Lane rightLane = new Lane("L2", MovementType.RIGHT);

        road.addLane(leftLane);
        road.addLane(rightLane);

        engine.getContext().getIntersection().addRoad(road);

        Vehicle vehicle = new Car(
            "C2", 10.0, MovementType.LEFT
        );

        VehicleArrivalEvent event = new VehicleArrivalEvent(
            vehicle, road.getId(), 10.0, 1
        );

        event.executeEvent(engine.getContext());

        assertSame(vehicle, leftLane.getNextVehicle());
        assertTrue(rightLane.isEmpty());
    }

    @Test
    void shouldAssignVehicleToLeastOccupiedCompatibleLane() {
        SimulationEngine engine = new SimulationEngine();
        registerRedNorthSignal(engine);

        Road road = new Road("R2", Direction.NORTH);

        Lane firstLane = new Lane("L1", MovementType.LEFT);
        Lane secondLane = new Lane("L2", MovementType.RIGHT);

        road.addLane(firstLane);
        road.addLane(secondLane);

        engine.getContext().getIntersection().addRoad(road);

        // Both lanes permit straight movement.
        firstLane.addVehicle(new Car("C3", 0));
        firstLane.addVehicle(new Bike("B1", 1));
        secondLane.addVehicle(new Car("C4", 2));

        Vehicle arrivingVehicle = new Car("C5", 10);

        VehicleArrivalEvent event = new VehicleArrivalEvent(
            arrivingVehicle, road.getId(), 10.0, 1
        );

        event.executeEvent(engine.getContext());

        assertSame(arrivingVehicle, secondLane.getVehicles().get(1));
        assertEquals(2, firstLane.getVehicleCount());
        assertEquals(2, secondLane.getVehicleCount());
    }

    @Test
    void shouldRejectArrivalWhenNoCompatibleLaneExists() {
        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R3", Direction.NORTH);

        Lane leftLane = new Lane("L1", MovementType.LEFT);

        road.addLane(leftLane);
        engine.getContext().getIntersection().addRoad(road);

        Vehicle vehicle = new Car(
            "C6", 10.0, MovementType.RIGHT
        );

        VehicleArrivalEvent event = new VehicleArrivalEvent(
            vehicle, road.getId(), 10.0, 1
        );

        assertThrows(
            IllegalStateException.class,
            () -> event.executeEvent(engine.getContext())
        );

        assertTrue(leftLane.isEmpty());
    }

    @Test
    void shouldRejectNullVehicle() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new VehicleArrivalEvent(
                null, "R1", 10.0, 1
            )
        );
    }

    @Test
    void shouldRejectInvalidRoadId() {
        Vehicle vehicle = new Car("C7", 10.0);

        assertThrows(
            IllegalArgumentException.class,
            () -> new VehicleArrivalEvent(
                vehicle, null, 10.0, 1
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new VehicleArrivalEvent(
                vehicle, " ", 10.0, 1
            )
        );
    }

    private void registerRedNorthSignal(SimulationEngine engine) {
        engine.getContext().addTrafficSignal(
            new TrafficSignal(Direction.NORTH)
        );
    }

    @Test
    void shouldScheduleMovementWhenVehicleArrivesAtGreenSignal()
            throws Exception {

        SimulationEngine engine = new SimulationEngine();

        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        road.addLane(lane);
        engine.getContext().getIntersection().addRoad(road);

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);

        engine.getContext().addTrafficSignal(signal);

        Vehicle vehicle = new Car("C10", 10.0);

        VehicleArrivalEvent event = new VehicleArrivalEvent(
            vehicle, road.getId(), 10.0, 1
        );

        event.executeEvent(engine.getContext());

        assertSame(vehicle, lane.getNextVehicle());
        assertEquals(1, engine.getScheduler().getEventCount());
    }
}
