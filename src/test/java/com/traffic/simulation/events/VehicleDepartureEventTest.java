
package com.traffic.simulation.events;

import com.traffic.control.Direction;
import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.Vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleDepartureEventTest {

    @Test
    void shouldRecordVehicleAsCompletedOnDeparture() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        Vehicle vehicle = new Car("C1", 10.0);

        VehicleDepartureEvent event = new VehicleDepartureEvent(
            vehicle,
            Direction.NORTH,
            20.0,
            1
        );

        event.executeEvent(context);

        assertEquals(1, context.getCompletedVehicleCount());
        assertSame(
            vehicle,
            context.getCompletedVehicles().get(0)
        );
    }

    @Test
    void shouldNotModifyCompletedVehiclesBeforeExecution() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        Vehicle vehicle = new Car("C1", 10.0);

        new VehicleDepartureEvent(
            vehicle,
            Direction.EAST,
            20.0,
            1
        );

        assertEquals(0, context.getCompletedVehicleCount());
    }

    @Test
    void shouldReturnCorrectVehicleId() {
        Vehicle vehicle = new Car("C2", 10.0);

        VehicleDepartureEvent event = new VehicleDepartureEvent(
            vehicle,
            Direction.SOUTH,
            20.0,
            1
        );

        assertEquals("C2", event.getVehicleId());
    }

    @Test
    void shouldReturnCorrectExitDirection() {
        Vehicle vehicle = new Car("C3", 10.0);

        VehicleDepartureEvent event = new VehicleDepartureEvent(
            vehicle,
            Direction.WEST,
            20.0,
            1
        );

        assertEquals(Direction.WEST, event.getExitDirection());
    }

    @Test
    void shouldRejectNullVehicle() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new VehicleDepartureEvent(
                null,
                Direction.NORTH,
                20.0,
                1
            )
        );
    }

    @Test
    void shouldRejectNullExitDirection() {
        Vehicle vehicle = new Car("C4", 10.0);

        assertThrows(
            IllegalArgumentException.class,
            () -> new VehicleDepartureEvent(
                vehicle,
                null,
                20.0,
                1
            )
        );
    }

    @Test
    void shouldRejectNullContext() {
        Vehicle vehicle = new Car("C5", 10.0);

        VehicleDepartureEvent event = new VehicleDepartureEvent(
            vehicle,
            Direction.NORTH,
            20.0,
            1
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> event.executeEvent(null)
        );
    }

    @Test
    void shouldNotRecordVehicleWhenContextIsNull() {
        Vehicle vehicle = new Car("C6", 10.0);

        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        VehicleDepartureEvent event = new VehicleDepartureEvent(
            vehicle,
            Direction.NORTH,
            20.0,
            1
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> event.executeEvent(null)
        );

        assertEquals(0, context.getCompletedVehicleCount());
    }

    @Test
    void shouldNotRecordVehicleTwiceWhenDepartureExecutesTwice() {

        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        Vehicle vehicle = new Car("C1", 10.0);

        VehicleDepartureEvent event = new VehicleDepartureEvent(
            vehicle,
            Direction.NORTH,
            20.0,
            1
        );

        event.executeEvent(context);
        event.executeEvent(context);

        assertEquals(1, context.getCompletedVehicleCount());
    }
}
