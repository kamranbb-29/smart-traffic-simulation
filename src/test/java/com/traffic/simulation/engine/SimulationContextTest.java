package com.traffic.simulation.engine;

import org.junit.jupiter.api.Test;

import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.Vehicle;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.events.MovementType;
import com.traffic.simulation.events.SignalFailureEvent;
import com.traffic.simulation.events.SimulationEvent;
import com.traffic.simulation.events.VehicleMovementEvent;
import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.control.Direction;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;

import static org.junit.jupiter.api.Assertions.*;
public class SimulationContextTest {
    @Test
    void getRoadReturnsCorrectRoad() {
        SimulationClock clock = new SimulationClock();
        Intersection intersection = new Intersection("INT1");
        Road road = new Road("R1");

        intersection.addRoad(road);

        SimulationContext context = new SimulationContext(clock, intersection);

        assertSame(road, context.getRoad("R1"));
    }

    @Test
    void getRoadThrowsExceptionForUnknownId() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> context.getRoad("R99")
        );
    }

    @Test
    void addTrafficSignalAndRetrieveIt() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);

        context.addTrafficSignal(signal);

        assertSame(signal, context.getTrafficSignal(Direction.NORTH));
    }

    @Test
    void addDuplicateTrafficSignalThrowsException() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal firstSignal = new TrafficSignal(Direction.NORTH);
        TrafficSignal secondSignal = new TrafficSignal(Direction.NORTH);

        context.addTrafficSignal(firstSignal);

        assertThrows(
            IllegalArgumentException.class,
            () -> context.addTrafficSignal(secondSignal)
        );
    }

    @Test
    void getTrafficSignalThrowsExceptionForUnknownDirection() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> context.getTrafficSignal(Direction.NORTH)
        );
    }

    @Test
    void addTrafficSignalWithNullDirectionThrowsException() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> context.addTrafficSignal(signal)
        );
    }

    @Test
    void failSignalMarksSignalAsFailed() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);

        context.failSignal(Direction.NORTH);

        assertTrue(context.isSignalFailed(Direction.NORTH));
    }

    @Test
    void recoverSignalRemovesFailedStatus() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        context.addTrafficSignal(signal);
        context.failSignal(Direction.NORTH);

        context.recoverSignal(Direction.NORTH);

        assertFalse(context.isSignalFailed(Direction.NORTH));
    }

    @Test
    void isSignalFailedThrowsExceptionForUnknownDirection() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> context.isSignalFailed(Direction.NORTH)
        );
    }

    @Test
    void recordCompletedVehicle() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        Vehicle vehicle = new Car("C1", 10.0);

        context.recordCompletedVehicle(vehicle);

        assertEquals(1, context.getCompletedVehicleCount());
        assertSame(vehicle, context.getCompletedVehicles().get(0));
    }

    @Test
    void recordNullVehicleThrowsException() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> context.recordCompletedVehicle(null)
        );
    }

    @Test
    void completedVehiclesListIsImmutable() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        Vehicle vehicle = new Car("C1", 10.0);
        context.recordCompletedVehicle(vehicle);

        assertThrows(
            UnsupportedOperationException.class,
            () -> context.getCompletedVehicles().clear()
        );
    }

    @Test
    void testScheduleEventDelegatesToScheduler() {
        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1"),
            scheduler
        );

        SimulationEvent event = new SignalFailureEvent(
            Direction.NORTH, 5.0, 1
        );

        context.scheduleEvent(event);

        assertEquals(1, scheduler.getEventCount());
    }

    @Test
    void testScheduleEventRejectsNullEvent() {
        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1"),
            scheduler
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> context.scheduleEvent(null)
        );
    }

    @Test
    void testScheduleEventWithoutSchedulerThrowsException() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        SimulationEvent event = new SignalFailureEvent(
            Direction.NORTH, 5.0, 1
        );

        assertThrows(
            IllegalStateException.class,
            () -> context.scheduleEvent(event)
        );
    }

    private Vehicle createTestVehicle(String id) {
        return new Vehicle(id, 0.0, MovementType.STRAIGHT) {
            @Override
            public String getType() {
                return "Test";
            }
        };
    }

    private SimulationContext createGreenContext(EventScheduler scheduler, boolean addVehicle, double headway) throws Exception {

        Intersection intersection = new Intersection("INT1");
        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        if (addVehicle) {
            lane.addVehicle(createTestVehicle("V1"));
        }

        road.addLane(lane);
        intersection.addRoad(road);

        TrafficSignal signal = new TrafficSignal(Direction.NORTH);
        signal.transitionTo(SignalState.GREEN);

        SimulationContext context = new SimulationContext(new SimulationClock(), intersection, scheduler, headway);

        context.addTrafficSignal(signal);

        return context;
    }
    @Test
    void shouldFindLaneUsingRoadAndLaneIds() {
        Intersection intersection = new Intersection("INT1");

        Road north = new Road("N", Direction.NORTH);
        Road south = new Road("S", Direction.SOUTH);

        Lane northLane = new Lane("L1");
        Lane southLane = new Lane("L1");

        north.addLane(northLane);
        south.addLane(southLane);

        intersection.addRoad(north);
        intersection.addRoad(south);

        SimulationContext context = new SimulationContext(
            new SimulationClock(), intersection
        );

        assertSame(northLane, context.getLane("N", "L1"));
        assertSame(southLane, context.getLane("S", "L1"));
    }

    @Test
    void shouldScheduleMovementForEligibleLane() throws Exception {
        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = createGreenContext(
            scheduler, true, 1.0
        );

        assertTrue(
            context.scheduleMovementForLane("R1", "L1", 5.0, 1)
        );

        assertEquals(1, scheduler.getEventCount());

        VehicleMovementEvent event =
            (VehicleMovementEvent) scheduler.getNextEvent();

        assertEquals("R1", event.getRoadId());
        assertEquals("L1", event.getLaneId());
    }

    @Test
    void shouldNotScheduleMovementForEmptyLane() throws Exception {
        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = createGreenContext(
            scheduler, false, 1.0
        );

        assertFalse(
            context.scheduleMovementForLane("R1", "L1", 5.0, 1)
        );

        assertEquals(0, scheduler.getEventCount());
    }

    @Test
    void shouldNotScheduleMovementForRedSignal() throws Exception {
        EventScheduler scheduler = new EventScheduler();

        Intersection intersection = new Intersection("INT1");
        Road road = new Road("R1", Direction.NORTH);
        Lane lane = new Lane("L1");

        lane.addVehicle(createTestVehicle("V1"));
        road.addLane(lane);
        intersection.addRoad(road);

        SimulationContext context = new SimulationContext(
            new SimulationClock(), intersection, scheduler
        );

        context.addTrafficSignal(new TrafficSignal(Direction.NORTH));

        assertFalse(
            context.scheduleMovementForLane("R1", "L1", 5.0, 1)
        );

        assertEquals(0, scheduler.getEventCount());
    }

    @Test
    void shouldNotScheduleMovementForFailedSignal() throws Exception {
        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = createGreenContext(
            scheduler, true, 1.0
        );

        context.failSignal(Direction.NORTH);

        assertFalse(
            context.scheduleMovementForLane("R1", "L1", 5.0, 1)
        );

        assertEquals(0, scheduler.getEventCount());
    }

    @Test
    void shouldNotScheduleMovementForBlockedLane() throws Exception {
        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = createGreenContext(
            scheduler, true, 1.0
        );

        context.getLane("R1", "L1").blockLane();

        assertFalse(
            context.scheduleMovementForLane("R1", "L1", 5.0, 1)
        );

        assertEquals(0, scheduler.getEventCount());
    }

    @Test
    void shouldNotScheduleMovementForBlockedRoad() throws Exception {
        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = createGreenContext(
            scheduler, true, 1.0
        );

        context.getRoad("R1").blockRoad();

        assertFalse(
            context.scheduleMovementForLane("R1", "L1", 5.0, 1)
        );

        assertEquals(0, scheduler.getEventCount());
    }

    @Test
    void shouldPreventDuplicateMovementEventsForSameLane()
            throws Exception {

        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = createGreenContext(
            scheduler, true, 1.0
        );

        assertTrue(
            context.scheduleMovementForLane("R1", "L1", 5.0, 1)
        );

        assertFalse(
            context.scheduleMovementForLane("R1", "L1", 6.0, 1)
        );

        assertEquals(1, scheduler.getEventCount());
    }

    @Test
    void shouldScheduleDifferentLanesWithSameIdOnDifferentRoads()
            throws Exception {

        EventScheduler scheduler = new EventScheduler();

        Intersection intersection = new Intersection("INT1");

        Road north = new Road("N", Direction.NORTH);
        Road south = new Road("S", Direction.SOUTH);

        Lane northLane = new Lane("L1");
        Lane southLane = new Lane("L1");

        northLane.addVehicle(createTestVehicle("N1"));
        southLane.addVehicle(createTestVehicle("S1"));

        north.addLane(northLane);
        south.addLane(southLane);

        intersection.addRoad(north);
        intersection.addRoad(south);

        SimulationContext context = new SimulationContext(
            new SimulationClock(), intersection, scheduler
        );

        TrafficSignal northSignal = new TrafficSignal(Direction.NORTH);
        TrafficSignal southSignal = new TrafficSignal(Direction.SOUTH);

        northSignal.transitionTo(SignalState.GREEN);
        southSignal.transitionTo(SignalState.GREEN);

        context.addTrafficSignal(northSignal);
        context.addTrafficSignal(southSignal);

        assertTrue(
            context.scheduleMovementForLane("N", "L1", 5.0, 1)
        );

        assertTrue(
            context.scheduleMovementForLane("S", "L1", 5.0, 1)
        );

        assertEquals(2, scheduler.getEventCount());
    }

    @Test
    void shouldScheduleNextVehicleAfterConfiguredHeadway()
            throws Exception {

        EventScheduler scheduler = new EventScheduler();

        SimulationContext context = createGreenContext(
            scheduler, true, 2.5
        );

        Lane lane = context.getLane("R1", "L1");
        lane.addVehicle(createTestVehicle("V2"));

        assertTrue(
            context.scheduleNextVehicle("R1", "L1", 10.0, 1)
        );

        VehicleMovementEvent event =
            (VehicleMovementEvent) scheduler.getNextEvent();

        assertEquals(12.5, event.getEventTime(), 0.000001);
    }

    @Test
    void shouldRejectInvalidHeadway() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new SimulationContext(
                new SimulationClock(),
                new Intersection("INT1"),
                new EventScheduler(),
                0.0
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new SimulationContext(
                new SimulationClock(),
                new Intersection("INT1"),
                new EventScheduler(),
                Double.NaN
            )
        );
    }

    @Test
    void shouldNotRecordSameVehicleTwice() {
        SimulationContext context = new SimulationContext(
            new SimulationClock(),
            new Intersection("INT1")
        );

        Vehicle vehicle = new Car("C1", 10.0);

        context.recordCompletedVehicle(vehicle);
        context.recordCompletedVehicle(vehicle);

        assertEquals(1, context.getCompletedVehicleCount());
        assertSame(vehicle, context.getCompletedVehicles().get(0));
    }
}
