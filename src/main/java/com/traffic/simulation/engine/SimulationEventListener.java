package com.traffic.simulation.engine;

import com.traffic.simulation.events.SimulationEvent;

/**
 * Listener interface for observing executed events within the discrete-event simulation engine.
 */
@FunctionalInterface
public interface SimulationEventListener {

    /**
     * Invoked immediately after an event has been executed by the simulation engine.
     *
     * @param event the event that was executed
     * @param context the simulation context in which the event executed
     */
    void onEventExecuted(SimulationEvent event, SimulationContext context);
}

