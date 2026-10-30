
package com.traffic.simulation.events;

import com.traffic.control.Direction;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;

import com.traffic.simulation.engine.SimulationClock;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.scheduler.EventScheduler;

import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.Vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleMovementEventTest {

    private static class Fixture {
        SimulationContext context;
        EventScheduler scheduler;
        Road road;
        Lane lane;
    }

    private Fixture createFixture(
            MovementType laneMovement,
            SignalState signalState,
            double headway,
            Vehicle... vehicles) throws Exception {

        Fixture fixture = new Fixture();

        Intersection intersection = new Intersection("INT1");

        fixture.road = new Road("R1", Direction.NORTH);
        fixture.lane = new Lane("L1", laneMovement);

        for (Vehicle vehicle : vehicles) {
            fixture.lane.addVehicle(vehicle);
        }

        fixture.road.addLane(fixture.lane);
        intersection.addRoad(fixture.road);

        fixture.scheduler = new EventScheduler();

        fixture.context = new SimulationContext(
            new SimulationClock(),
            intersection,
            fixture.scheduler,
            headway
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);

        if (signalState == SignalState.GREEN) {
            signal.transitionTo(SignalState.GREEN);
        }

        fixture.context.addTrafficSignal(signal);

        return fixture;
    }

    private VehicleMovementEvent createEvent(
            Fixture fixture,
            MovementType movement,
            double time) {

        return new VehicleMovementEvent(
            fixture.road.getId(),
            fixture.lane.getId(),
            Direction.NORTH,
            movement,
            time,
            1
        );
    }

    @Test
    void shouldScheduleDepartureAfterSuccessfulMovement()
            throws Exception {

        Vehicle vehicle = new Car(
            "C1", 0, MovementType.STRAIGHT
        );

        Fixture fixture = createFixture(
            MovementType.LEFT,
            SignalState.GREEN,
            1.0,
            vehicle
        );

        createEvent(
            fixture, MovementType.STRAIGHT, 5.0
        ).executeEvent(fixture.context);

        assertTrue(fixture.lane.isEmpty());

        // Movement itself must not record completion.
        assertEquals(0, fixture.context.getCompletedVehicleCount());

        assertEquals(1, fixture.scheduler.getEventCount());

        SimulationEvent departure =
            fixture.scheduler.getNextEvent();

        assertInstanceOf(VehicleDepartureEvent.class, departure);

        assertEquals(5.0, departure.getEventTime(), 0.000001);

        departure.executeEvent(fixture.context);

        assertEquals(1, fixture.context.getCompletedVehicleCount());
        assertSame(
            vehicle,
            fixture.context.getCompletedVehicles().get(0)
        );
    }

    @Test
    void shouldScheduleDepartureAndNextVehicleAfterHeadway()
            throws Exception {

        Vehicle first = new Car("C1", 0, MovementType.STRAIGHT);
        Vehicle second = new Car("C2", 1, MovementType.STRAIGHT);

        Fixture fixture = createFixture(
            MovementType.LEFT,
            SignalState.GREEN,
            2.5,
            first,
            second
        );

        createEvent(
            fixture, MovementType.STRAIGHT, 5.0
        ).executeEvent(fixture.context);

        assertEquals(1, fixture.lane.getVehicleCount());
        assertSame(second, fixture.lane.getNextVehicle());
        assertEquals(0, fixture.context.getCompletedVehicleCount());

        assertEquals(2, fixture.scheduler.getEventCount());

        SimulationEvent departure =
            fixture.scheduler.getNextEvent();

        assertInstanceOf(VehicleDepartureEvent.class, departure);
        assertEquals(5.0, departure.getEventTime(), 0.000001);

        departure.executeEvent(fixture.context);

        assertEquals(1, fixture.context.getCompletedVehicleCount());

        SimulationEvent next =
            fixture.scheduler.getNextEvent();

        assertInstanceOf(VehicleMovementEvent.class, next);

        VehicleMovementEvent nextMovement =
            (VehicleMovementEvent) next;

        assertEquals(7.5, nextMovement.getEventTime(), 0.000001);
        assertEquals("R1", nextMovement.getRoadId());
        assertEquals("L1", nextMovement.getLaneId());
    }

    @Test
    void shouldKeepVehicleInLaneWhenSignalIsRed() throws Exception {
        Vehicle vehicle = new Car("C1", 0);

        Fixture fixture = createFixture(
            MovementType.STRAIGHT,
            SignalState.RED,
            1.0,
            vehicle
        );

        createEvent(
            fixture, MovementType.STRAIGHT, 5.0
        ).executeEvent(fixture.context);

        assertSame(vehicle, fixture.lane.getNextVehicle());
        assertEquals(0, fixture.context.getCompletedVehicleCount());
    }

    @Test
    void shouldKeepVehicleInLaneWhenSignalHasFailed()
            throws Exception {

        Vehicle vehicle = new Car("C1", 0);

        Fixture fixture = createFixture(
            MovementType.STRAIGHT,
            SignalState.GREEN,
            1.0,
            vehicle
        );

        fixture.context.failSignal(Direction.NORTH);

        createEvent(
            fixture, MovementType.STRAIGHT, 5.0
        ).executeEvent(fixture.context);

        assertSame(vehicle, fixture.lane.getNextVehicle());
        assertEquals(0, fixture.context.getCompletedVehicleCount());
    }

    @Test
    void shouldKeepVehicleInLaneWhenLaneIsBlocked()
            throws Exception {

        Vehicle vehicle = new Car("C1", 0);

        Fixture fixture = createFixture(
            MovementType.STRAIGHT,
            SignalState.GREEN,
            1.0,
            vehicle
        );

        fixture.lane.blockLane();

        createEvent(
            fixture, MovementType.STRAIGHT, 5.0
        ).executeEvent(fixture.context);

        assertSame(vehicle, fixture.lane.getNextVehicle());
        assertEquals(0, fixture.context.getCompletedVehicleCount());
    }

    @Test
    void shouldKeepVehicleInLaneWhenRoadIsBlocked()
            throws Exception {

        Vehicle vehicle = new Car("C1", 0);

        Fixture fixture = createFixture(
            MovementType.STRAIGHT,
            SignalState.GREEN,
            1.0,
            vehicle
        );

        fixture.road.blockRoad();

        createEvent(
            fixture, MovementType.STRAIGHT, 5.0
        ).executeEvent(fixture.context);

        assertSame(vehicle, fixture.lane.getNextVehicle());
        assertEquals(0, fixture.context.getCompletedVehicleCount());
    }

    @Test
    void shouldThrowWhenEventMovementDoesNotMatchVehicleIntent()
            throws Exception {

        Vehicle vehicle = new Car("C1", 0, MovementType.RIGHT);

        Fixture fixture = createFixture(
            MovementType.RIGHT,
            SignalState.GREEN,
            1.0,
            vehicle
        );

        assertThrows(
            IllegalStateException.class,
            () -> createEvent(
                fixture, MovementType.STRAIGHT, 5.0
            ).executeEvent(fixture.context)
        );

        assertSame(vehicle, fixture.lane.getNextVehicle());
        assertEquals(0, fixture.context.getCompletedVehicleCount());
    }

    @Test
    void shouldThrowWhenLaneDoesNotPermitVehicleMovement()
            throws Exception {

        Vehicle vehicle = new Car("C1", 0, MovementType.RIGHT);

        Fixture fixture = createFixture(
            MovementType.LEFT,
            SignalState.GREEN,
            1.0,
            vehicle
        );

        assertThrows(
            IllegalStateException.class,
            () -> createEvent(
                fixture, MovementType.RIGHT, 5.0
            ).executeEvent(fixture.context)
        );

        assertSame(vehicle, fixture.lane.getNextVehicle());
        assertEquals(0, fixture.context.getCompletedVehicleCount());
    }

    @Test
    void shouldRejectEventWithWrongApproachDirection()
            throws Exception {

        Vehicle vehicle = new Car("C1", 0);

        Fixture fixture = createFixture(
            MovementType.STRAIGHT,
            SignalState.GREEN,
            1.0,
            vehicle
        );

        VehicleMovementEvent event = new VehicleMovementEvent(
            "R1",
            "L1",
            Direction.SOUTH,
            MovementType.STRAIGHT,
            5.0,
            1
        );

        assertThrows(
            IllegalStateException.class,
            () -> event.executeEvent(fixture.context)
        );

        assertSame(vehicle, fixture.lane.getNextVehicle());
    }

    @Test
    void shouldHandleEmptyLaneWithoutCompletingVehicle()
            throws Exception {

        Fixture fixture = createFixture(
            MovementType.STRAIGHT,
            SignalState.GREEN,
            1.0
        );

        createEvent(
            fixture, MovementType.STRAIGHT, 5.0
        ).executeEvent(fixture.context);

        assertTrue(fixture.lane.isEmpty());
        assertEquals(0, fixture.context.getCompletedVehicleCount());
        assertEquals(0, fixture.scheduler.getEventCount());
    }

    @Test
    void shouldReleasePendingMarkerAfterBlockedMovement()
            throws Exception {

        Vehicle vehicle = new Car("C1", 0);

        Fixture fixture = createFixture(
            MovementType.STRAIGHT,
            SignalState.GREEN,
            1.0,
            vehicle
        );

        assertTrue(
            fixture.context.scheduleMovementForLane(
                "R1", "L1", 5.0, 1
            )
        );

        VehicleMovementEvent event =
            (VehicleMovementEvent) fixture.scheduler.getNextEvent();

        fixture.lane.blockLane();

        event.executeEvent(fixture.context);

        assertSame(vehicle, fixture.lane.getNextVehicle());
        assertEquals(0, fixture.context.getCompletedVehicleCount());

        fixture.lane.unblockLane();

        assertTrue(
            fixture.context.scheduleMovementForLane(
                "R1", "L1", 6.0, 1
            )
        );
    }

    @Test
    void shouldRejectNullContext() {
        VehicleMovementEvent event = new VehicleMovementEvent(
            "R1",
            "L1",
            Direction.NORTH,
            MovementType.STRAIGHT,
            5.0,
            1
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> event.executeEvent(null)
        );
    }
}
