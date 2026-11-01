
package com.traffic.simulation.scheduler;

import com.traffic.simulation.events.SimulationEvent;
import java.util.OptionalDouble;

public interface EventScheduling {

    void scheduleEvent(SimulationEvent event);

    boolean hasPendingArrivalEvents();

    OptionalDouble getNextArrivalEventTime();

    boolean hasPendingRecoveryEvents();

    boolean hasPendingDepartureEvents();

    boolean hasPendingProgressEvents();

    int getPendingArrivalCount();

    int getPendingDepartureCount();
}
