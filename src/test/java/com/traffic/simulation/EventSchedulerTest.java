package com.traffic.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.simulation.events.VehicleArrivalEvent;
import com.traffic.simulation.scheduler.EventScheduler;

public class EventSchedulerTest{
    @Test
    void testEventOrderingByTime() {
        EventScheduler scheduler = new EventScheduler();
        scheduler.scheduleEvent(new VehicleArrivalEvent("A", 30, 1));
        scheduler.scheduleEvent(new VehicleArrivalEvent("B", 10, 1));
        scheduler.scheduleEvent(new VehicleArrivalEvent("C", 20, 1));

        assertEquals("B", ((VehicleArrivalEvent) scheduler.getNextEvent()).getVehicleId());
        assertEquals("C", ((VehicleArrivalEvent) scheduler.getNextEvent()).getVehicleId());
        assertEquals("A", ((VehicleArrivalEvent) scheduler.getNextEvent()).getVehicleId());
    }

    @Test 
    void testEventOrderingByPriority(){
        EventScheduler scheduler = new EventScheduler();

        scheduler.scheduleEvent(new VehicleArrivalEvent("A", 10,3));
        scheduler.scheduleEvent(new VehicleArrivalEvent("B", 10, 1));

        assertEquals("B", ((VehicleArrivalEvent) scheduler.getNextEvent()).getVehicleId());
        assertEquals("A", ((VehicleArrivalEvent) scheduler.getNextEvent()).getVehicleId());
    }

    @Test
    void testFifoWhenTimeAndPriorityAreEqual() {
        EventScheduler scheduler = new EventScheduler();

        scheduler.scheduleEvent(new VehicleArrivalEvent("A", 10, 1));
        scheduler.scheduleEvent(new VehicleArrivalEvent("B", 10, 1));
        scheduler.scheduleEvent(new VehicleArrivalEvent("C", 10, 1));

        assertEquals("A",
            ((VehicleArrivalEvent) scheduler.getNextEvent()).getVehicleId());

        assertEquals("B",
            ((VehicleArrivalEvent) scheduler.getNextEvent()).getVehicleId());

        assertEquals("C",
            ((VehicleArrivalEvent) scheduler.getNextEvent()).getVehicleId());
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

        scheduler.scheduleEvent(new VehicleArrivalEvent("A", 10,1));
        scheduler.scheduleEvent(new VehicleArrivalEvent("B", 20, 1));
        
        assertTrue(scheduler.hasEvents());
        assertEquals(2, scheduler.getEventCount());

        scheduler.getNextEvent();

        assertEquals(1, scheduler.getEventCount());
    }

    @Test 
    void testClearEvents(){
        EventScheduler scheduler = new EventScheduler();

        scheduler.scheduleEvent(new VehicleArrivalEvent("A", 10, 1));
        scheduler.scheduleEvent(new VehicleArrivalEvent("B", 20,1));

        scheduler.clearEvents();

        assertFalse(scheduler.hasEvents());
        assertEquals(0, scheduler.getEventCount());
        assertNull(scheduler.getNextEvent());
    }
}
