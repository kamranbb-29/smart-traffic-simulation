package com.traffic.simulation.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.events.VehicleArrivalEvent;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.events.SimulationEvent;
import com.traffic.traffic.intersection.Intersection;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngineTest {
    public static class RecordingEvent extends SimulationEvent {
        private final List<String> executionOrder;
        private final String id;

        public RecordingEvent(String id, double eventTime,
                            int eventPriority, List<String> executionOrder) {
            super(eventTime, "Recording_Event", eventPriority);
            this.id = id;
            this.executionOrder = executionOrder;
        }

        @Override
        public void executeEvent(SimulationContext context) {
            executionOrder.add(id);
        }
    }
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
    void testReset(){
        SimulationEngine engine = new SimulationEngine();

        engine.getScheduler().scheduleEvent(new TestEvent("A", 10, 1));
        engine.reset();

        assertEquals(0.0, engine.getClock().getCurrentTime());
        assertFalse(engine.isRunning());
        assertFalse(engine.getScheduler().hasEvents());
    }

    @Test
    void defaultConstructorCreatesINT1() {
        SimulationEngine engine = new SimulationEngine();

        assertNotNull(engine.getContext().getIntersection());
        assertEquals("INT1", engine.getContext().getIntersection().getId());
    }

    @Test
    void parameterizedConstructorUsesProvidedIntersection() {
        Intersection intersection = new Intersection("INT2");

        SimulationEngine engine = new SimulationEngine(intersection);

        assertSame(intersection, engine.getContext().getIntersection());
    }

    @Test
    void constructorRejectsNullIntersection() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new SimulationEngine(null)
        );
    }

    @Test
    void testRunExecutesEvents() {
        SimulationEngine engine = new SimulationEngine();
        List<String> executionOrder = new ArrayList<>();

        engine.getScheduler().scheduleEvent(
            new RecordingEvent("A", 10, 1, executionOrder)
        );
        engine.getScheduler().scheduleEvent(
            new RecordingEvent("B", 20, 1, executionOrder)
        );

        engine.run();

        assertEquals(List.of("A", "B"), executionOrder);
        assertFalse(engine.isRunning());
    }

    @Test
    void testRunAdvancesClock() {
        SimulationEngine engine = new SimulationEngine();

        engine.getScheduler().scheduleEvent(new TestEvent("A", 10, 1));
        engine.getScheduler().scheduleEvent(new TestEvent("B", 25, 1));
        engine.getScheduler().scheduleEvent(new TestEvent("C", 40, 1));

        engine.run();

        assertEquals(40.0, engine.getClock().getCurrentTime());
    }

    @Test
    void testStop() {
        SimulationEngine engine = new SimulationEngine();
        List<String> executionOrder = new ArrayList<>();

        RecordingEvent first = new RecordingEvent("A", 10, 1, executionOrder) {
            @Override
            public void executeEvent(SimulationContext context) {
                executionOrder.add("A");
                engine.stop();
            }
        };

        engine.getScheduler().scheduleEvent(first);
        engine.getScheduler().scheduleEvent(
            new RecordingEvent("B", 20, 1, executionOrder)
        );

        engine.run();

        assertEquals(List.of("A"), executionOrder);
        assertFalse(engine.isRunning());
        assertTrue(engine.getScheduler().hasEvents());
    }

    @Test
    void testExceptionHandling() {
        SimulationEngine engine = new SimulationEngine();

        SimulationEvent failingEvent = new SimulationEvent(10, "Failing_Event", 1) {
            @Override
            public void executeEvent(SimulationContext context) {
                throw new RuntimeException("Test exception");
            }
        };

        engine.getScheduler().scheduleEvent(failingEvent);

        assertThrows(RuntimeException.class, engine::run);
        assertFalse(engine.isRunning());
    }

    @Test
    void testRunWithEmptyScheduler() {
        SimulationEngine engine = new SimulationEngine();

        assertDoesNotThrow(engine::run);

        assertFalse(engine.isRunning());
        assertEquals(0.0, engine.getClock().getCurrentTime());
    }
}
