package com.traffic.simulation.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.events.VehicleArrivalEvent;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.events.SimulationEvent;
public class SimulationEngineTest {
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
}
