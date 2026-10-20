package com.traffic.simulation.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.events.VehicleArrivalEvent;
public class SimulationEngineTest {
    @Test 
    void testReset(){
        SimulationEngine engine = new SimulationEngine();

        engine.getScheduler().scheduleEvent(new VehicleArrivalEvent("A", 10, 1));
        engine.reset();

        assertEquals(0.0, engine.getClock().getCurrentTime());
        assertFalse(engine.isRunning());
        assertFalse(engine.getScheduler().hasEvents());
    }
}
