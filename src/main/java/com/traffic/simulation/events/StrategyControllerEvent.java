
package com.traffic.simulation.events;

import com.traffic.control.Direction;
import com.traffic.control.SignalControlStrategy;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSnapshot;
import com.traffic.simulation.engine.SimulationContext;

public class StrategyControllerEvent extends SimulationEvent {

    private final SignalControlStrategy strategy;
    private final int remainingPhases;

    private static final double GREEN_DURATION = 8.0;
    private static final double PHASE_DURATION = 10.0;

    public StrategyControllerEvent(
            SignalControlStrategy strategy,
            int remainingPhases,
            double eventTime,
            int eventPriority) {

        super(eventTime, "Strategy_Controller", eventPriority);

        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }

        if (remainingPhases <= 0) {
            throw new IllegalArgumentException(
                    "Remaining phases must be positive");
        }

        this.strategy = strategy;
        this.remainingPhases = remainingPhases;
    }

    @Override
    public void executeEvent(SimulationContext context) {

        // Stop scheduling phases if no vehicles remain.
        if (context.getIntersection().getVehicleCount() == 0
                && !context.hasPendingArrivalEvents()) {

            System.out.println(
                    "\n[CONTROLLER] All vehicles completed. Simulation finished.");

            return;
        }

        TrafficSnapshot snapshot = new TrafficSnapshot(
                context.getIntersection().getCurrentTrafficState());

        Direction selectedDirection = strategy.chooseDirection(snapshot);

        double currentTime = getEventTime();

        System.out.println(
                "\n[CONTROLLER] Selected direction: " + selectedDirection
                        + " at time " + currentTime);

        context.scheduleEvent(new TrafficLightChangeEvent(
                selectedDirection, SignalState.GREEN, currentTime, 1));

        context.scheduleEvent(new TrafficLightChangeEvent(
                selectedDirection, SignalState.YELLOW,
                currentTime + GREEN_DURATION, 1));

        context.scheduleEvent(new TrafficLightChangeEvent(
                selectedDirection, SignalState.RED,
                currentTime + PHASE_DURATION, 1));

        if (remainingPhases > 1) {
            context.scheduleEvent(new StrategyControllerEvent(
                    strategy, remainingPhases - 1,
                    currentTime + PHASE_DURATION, 1));
        }
    }
}