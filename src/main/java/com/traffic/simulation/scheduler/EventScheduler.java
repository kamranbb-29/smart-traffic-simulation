package com.traffic.simulation.scheduler;

import com.traffic.simulation.events.EmergencyArrivalEvent;
import com.traffic.simulation.events.SimulationEvent;
import com.traffic.simulation.events.VehicleArrivalEvent;

import java.util.OptionalDouble;
import java.util.PriorityQueue;

public class EventScheduler implements EventScheduling {
    private final PriorityQueue<ScheduledEvent> eventQueue;
    private long nextSequenceNumber = 0;

    public EventScheduler() {
        this.eventQueue = new PriorityQueue<>();
    }

    public void scheduleEvent(SimulationEvent event) {
        ScheduledEvent scheduled = new ScheduledEvent(event, nextSequenceNumber++);
        eventQueue.add(scheduled);
    }

    public SimulationEvent getNextEvent() {
        ScheduledEvent scheduled = eventQueue.poll();

        if (scheduled == null) {
            return null;
        }

        return scheduled.getEvent();
    }

    public boolean hasEvents() {
        return !eventQueue.isEmpty();
    }

    public int getEventCount() {
        return eventQueue.size();
    }

    public void clearEvents() {
        eventQueue.clear();
    }

    @Override
    public boolean hasPendingArrivalEvents() {
        return eventQueue.stream()
                .anyMatch(scheduled -> scheduled.getEvent().isArrivalEvent());
    }

    @Override
    public OptionalDouble getNextArrivalEventTime() {
        return eventQueue.stream()
                .map(ScheduledEvent::getEvent)
                .filter(SimulationEvent::isArrivalEvent)
                .mapToDouble(SimulationEvent::getEventTime)
                .min();
    }

    @Override
    public boolean hasPendingRecoveryEvents() {
        return eventQueue.stream()
                .anyMatch(scheduled -> scheduled.getEvent().isRecoveryEvent());
    }

    @Override
    public boolean hasPendingDepartureEvents() {
        return eventQueue.stream()
                .anyMatch(scheduled -> scheduled.getEvent().isDepartureEvent());
    }

    @Override
    public boolean hasPendingProgressEvents() {
        return eventQueue.stream()
                .anyMatch(scheduled -> scheduled.getEvent().canEnableProgress());
    }

    @Override
    public int getPendingArrivalCount() {
        return (int) eventQueue.stream()
                .filter(scheduled -> scheduled.getEvent().isArrivalEvent())
                .count();
    }

    @Override
    public int getPendingDepartureCount() {
        return (int) eventQueue.stream()
                .filter(scheduled -> scheduled.getEvent().isDepartureEvent())
                .count();
    }

    public java.util.List<com.traffic.simulation.events.VehicleDepartureEvent> getPendingDepartureEvents() {
        return eventQueue.stream()
                .map(ScheduledEvent::getEvent)
                .filter(e -> e instanceof com.traffic.simulation.events.VehicleDepartureEvent)
                .map(e -> (com.traffic.simulation.events.VehicleDepartureEvent) e)
                .toList();
    }

    public boolean hasPendingControllerEvents() {
        return eventQueue.stream()
                .anyMatch(scheduled -> scheduled.getEvent() instanceof com.traffic.simulation.events.StrategyControllerEvent);
    }
}
