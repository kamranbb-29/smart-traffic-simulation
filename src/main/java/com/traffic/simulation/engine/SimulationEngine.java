package com.traffic.simulation.engine;

import com.traffic.simulation.events.SimulationEvent;
import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.traffic.intersection.Intersection;

public class SimulationEngine {
    public static final int DEFAULT_MAX_EVENT_LIMIT = 500_000;

    private final SimulationContext context;
    private final EventScheduler scheduler;
    private volatile boolean isRunning;
    private volatile boolean manuallyStopped;
    private int maxEventLimit;
    private int processedEventsCount;
    private SimulationTerminationStatus terminationStatus;
    private final java.util.List<SimulationEventListener> listeners;

    public SimulationEngine() {
        this(new Intersection("INT1"), DEFAULT_MAX_EVENT_LIMIT);
    }

    public SimulationEngine(Intersection intersection) {
        this(intersection, DEFAULT_MAX_EVENT_LIMIT);
    }

    public SimulationEngine(Intersection intersection, int maxEventLimit) {
        if (intersection == null) {
            throw new IllegalArgumentException("Intersection cannot be null");
        }
        if (maxEventLimit <= 0) {
            throw new IllegalArgumentException("Max event limit must be positive");
        }

        this.scheduler = new EventScheduler();
        this.context = new SimulationContext(
                new SimulationClock(),
                intersection,
                scheduler
        );
        this.isRunning = false;
        this.manuallyStopped = false;
        this.maxEventLimit = maxEventLimit;
        this.processedEventsCount = 0;
        this.terminationStatus = null;
        this.listeners = new java.util.ArrayList<>();
    }

    public SimulationTerminationStatus run() {
        isRunning = true;
        manuallyStopped = false;

        try {
            while (isRunning) {
                // 1. Safety limit: maximum processed events
                if (processedEventsCount >= maxEventLimit) {
                    return recordTermination(SimulationTerminationStatus.LOOP_LIMIT_EXHAUSTED);
                }

                // 2. Genuine deadlock / stall detection
                if (isStalled()) {
                    return recordTermination(SimulationTerminationStatus.STALLED_DEADLOCK);
                }

                // 3. Check if scheduler has any events left
                if (!scheduler.hasEvents()) {
                    if (context.getIntersection().getVehicleCount() > 0) {
                        if (context.isPhaseBudgetExhausted()) {
                            return recordTermination(SimulationTerminationStatus.PHASE_LIMIT_EXHAUSTED);
                        } else {
                            return recordTermination(SimulationTerminationStatus.STALLED_DEADLOCK);
                        }
                    } else {
                        return recordTermination(SimulationTerminationStatus.NORMAL_COMPLETION);
                    }
                }

                // 4. Poll and execute the next event
                SimulationEvent event = scheduler.getNextEvent();
                processedEventsCount++;
                context.getClock().advanceTime(event.getEventTime());
                event.executeEvent(context);

                for (SimulationEventListener listener : listeners) {
                    listener.onEventExecuted(event, context);
                }

                // 5. Check if manual stop was invoked during event execution
                if (manuallyStopped) {
                    return recordTermination(SimulationTerminationStatus.MANUALLY_STOPPED);
                }
            }

            // Exited while loop because isRunning was cleared
            if (manuallyStopped || !isRunning) {
                return recordTermination(SimulationTerminationStatus.MANUALLY_STOPPED);
            }

            if (terminationStatus == null) {
                recordTermination(SimulationTerminationStatus.NORMAL_COMPLETION);
            }
            return terminationStatus;
        } finally {
            isRunning = false;
            this.context.closeActiveGreenIntervals(this.context.getClock().getCurrentTime());
        }
    }

    private SimulationTerminationStatus recordTermination(SimulationTerminationStatus status) {
        this.terminationStatus = status;
        this.context.setTerminationStatus(status);
        this.context.closeActiveGreenIntervals(this.context.getClock().getCurrentTime());
        return status;
    }

    public boolean isStalled() {
        // If no vehicles remain in the intersection, it cannot be in a vehicle deadlock
        if (context.getIntersection().getVehicleCount() == 0) {
            return false;
        }

        // Departures in flight must be allowed to complete
        if (context.hasPendingDepartureEvents()) {
            return false;
        }

        // Future arrivals can introduce traffic and enable future progress
        if (context.hasPendingArrivalEvents()) {
            return false;
        }

        // Future recovery events can unblock vehicles or signals
        if (context.hasPendingRecoveryEvents()) {
            return false;
        }

        // If all remaining vehicles in the intersection are physically blocked (road/lane blocked or signal failed),
        // and no arrival or recovery events exist, the simulation cannot make progress.
        // Recurring controller events alone cannot clear blockages.
        return context.areAllVehiclesBlocked();
    }

    public void stop() {
        manuallyStopped = true;
        isRunning = false;
    }

    public void reset() {
        isRunning = false;
        manuallyStopped = false;
        context.getClock().reset();
        scheduler.clearEvents();
        context.clearScheduledMovementEvents();
        context.resetProgress();
        processedEventsCount = 0;
        terminationStatus = null;
    }

    public SimulationContext getContext() {
        return context;
    }

    public SimulationClock getClock() {
        return context.getClock();
    }

    public EventScheduler getScheduler() {
        return scheduler;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public SimulationTerminationStatus getTerminationStatus() {
        return terminationStatus;
    }

    public void setMaxEventLimit(int maxEventLimit) {
        if (maxEventLimit <= 0) {
            throw new IllegalArgumentException("Max event limit must be positive");
        }
        this.maxEventLimit = maxEventLimit;
    }

    public int getMaxEventLimit() {
        return maxEventLimit;
    }

    public int getProcessedEventsCount() {
        return processedEventsCount;
    }

    public void addEventListener(SimulationEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        this.listeners.add(listener);
    }

    public void removeEventListener(SimulationEventListener listener) {
        this.listeners.remove(listener);
    }

    public void clearEventListeners() {
        this.listeners.clear();
    }
}
