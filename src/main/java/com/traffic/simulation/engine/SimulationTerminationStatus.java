package com.traffic.simulation.engine;

/**
 * Represents the final termination status of a traffic simulation execution.
 */
public enum SimulationTerminationStatus {
    /**
     * All introduced vehicles have completed their journeys and no relevant arrivals remain.
     */
    NORMAL_COMPLETION,

    /**
     * The configured active signal-phase budget has been exhausted while work remains.
     */
    PHASE_LIMIT_EXHAUSTED,

    /**
     * Vehicles remain unfinished, but no event capable of enabling progress remains.
     */
    STALLED_DEADLOCK,

    /**
     * The maximum event-processing safety limit has been reached.
     */
    LOOP_LIMIT_EXHAUSTED,

    /**
     * The simulation was explicitly stopped by the user or caller.
     */
    MANUALLY_STOPPED
}

