package com.traffic.simulation.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.simulation.events.EmergencyArrivalEvent;
import com.traffic.simulation.events.VehicleArrivalEvent;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.EmergencyVehicle;
import java.util.OptionalDouble;
import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.events.SimulationEvent;
public class EventSchedulerTest{
    public static class TestEvent extends SimulationEvent{
        private final String id;

        public TestEvent(String id, double eventTime, int eventPriority){
            super(eventTime, "Test_Event", eventPriority);
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public void executeEvent(SimulationContext context) {
            //No operation for test
        }
    }
    @Test
    void testEventOrderingByTime() {
        EventScheduler scheduler = new EventScheduler();
        scheduler.scheduleEvent(new TestEvent("A", 30, 1));
        scheduler.scheduleEvent(new TestEvent("B", 10, 1));
        scheduler.scheduleEvent(new TestEvent("C", 20, 1));

        assertEquals("B", ((TestEvent) scheduler.getNextEvent()).getId());
        assertEquals("C", ((TestEvent) scheduler.getNextEvent()).getId());
        assertEquals("A", ((TestEvent) scheduler.getNextEvent()).getId());
    }

    @Test 
    void testEventOrderingByPriority(){
        EventScheduler scheduler = new EventScheduler();

        scheduler.scheduleEvent(new TestEvent("A", 10,3));
        scheduler.scheduleEvent(new TestEvent("B", 10, 1));

        assertEquals("B", ((TestEvent) scheduler.getNextEvent()).getId());
        assertEquals("A", ((TestEvent) scheduler.getNextEvent()).getId());
    }

    @Test
    void testFifoWhenTimeAndPriorityAreEqual() {
        EventScheduler scheduler = new EventScheduler();

        scheduler.scheduleEvent(new TestEvent("A", 10, 1));
        scheduler.scheduleEvent(new TestEvent("B", 10, 1));
        scheduler.scheduleEvent(new TestEvent("C", 10, 1));

        assertEquals("A", ((TestEvent) scheduler.getNextEvent()).getId());
        assertEquals("B", ((TestEvent) scheduler.getNextEvent()).getId());
        assertEquals("C", ((TestEvent) scheduler.getNextEvent()).getId());
    }

    @Test 
    void testEmptyScheduler(){
        EventScheduler scheduler = new EventScheduler();

        assertFalse(scheduler.hasEvents());
        assertEquals(0, scheduler.getEventCount());
        assertNull(scheduler.getNextEvent());
    }

    @Test 
    void testEventCount(){
        EventScheduler scheduler = new EventScheduler();

        scheduler.scheduleEvent(new TestEvent("A", 10,1));
        scheduler.scheduleEvent(new TestEvent("B", 20, 1));
        
        assertTrue(scheduler.hasEvents());
        assertEquals(2, scheduler.getEventCount());

        scheduler.getNextEvent();

        assertEquals(1, scheduler.getEventCount());
    }

    @Test 
    void testClearEvents(){
        EventScheduler scheduler = new EventScheduler();

        scheduler.scheduleEvent(new TestEvent("A", 10, 1));
        scheduler.scheduleEvent(new TestEvent("B", 20,1));

        scheduler.clearEvents();

        assertFalse(scheduler.hasEvents());
        assertEquals(0, scheduler.getEventCount());
        assertNull(scheduler.getNextEvent());
    }

    @Test
    @DisplayName("Next arrival inspection: non-destructively returns earliest arrival across VehicleArrivalEvent and EmergencyArrivalEvent")
    void testGetNextArrivalEventTimeNonDestructive() {
        EventScheduler scheduler = new EventScheduler();

        // Initially no arrivals
        assertTrue(scheduler.getNextArrivalEventTime().isEmpty());
        assertFalse(scheduler.hasPendingArrivalEvents());

        // Add non-arrival events
        scheduler.scheduleEvent(new TestEvent("NON-ARR-1", 5.0, 1));
        scheduler.scheduleEvent(new TestEvent("NON-ARR-2", 30.0, 1));
        assertTrue(scheduler.getNextArrivalEventTime().isEmpty());
        assertFalse(scheduler.hasPendingArrivalEvents());

        // Add VehicleArrivalEvent at t=25.0
        VehicleArrivalEvent vEvent = new VehicleArrivalEvent(new Car("C1", 25.0), "ROAD-NORTH", 25.0, 1);
        scheduler.scheduleEvent(vEvent);
        assertTrue(scheduler.hasPendingArrivalEvents());
        assertEquals(25.0, scheduler.getNextArrivalEventTime().orElseThrow(), 1e-9);

        // Add EmergencyArrivalEvent at t=15.0 (earlier than vEvent)
        EmergencyArrivalEvent emEvent = new EmergencyArrivalEvent(new EmergencyVehicle("AMB1", 15.0), "LANE-EAST", 15.0, 1);
        scheduler.scheduleEvent(emEvent);
        assertTrue(scheduler.hasPendingArrivalEvents());
        assertEquals(15.0, scheduler.getNextArrivalEventTime().orElseThrow(), 1e-9);

        // Verify non-destructive inspection: event queue size must still be 4
        assertEquals(4, scheduler.getEventCount(), "Queue size must remain 4 after inspection");
        assertEquals("NON-ARR-1", ((TestEvent) scheduler.getNextEvent()).getId(), "First event at t=5.0 is polled correctly");
        assertSame(emEvent, scheduler.getNextEvent(), "Earliest arrival at t=15.0 is polled correctly");
        assertEquals(2, scheduler.getEventCount(), "Two events remain");
    }
}
