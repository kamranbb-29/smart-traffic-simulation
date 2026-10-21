package com.traffic.simulation.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.simulation.events.VehicleArrivalEvent;
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
}
