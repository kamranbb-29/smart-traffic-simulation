package com.traffic.benchmark;

import com.traffic.control.Direction;
import com.traffic.control.DirectionTraffic;
import com.traffic.control.SignalControlStrategy;
import com.traffic.control.SignalState;
import com.traffic.control.TrafficSignal;
import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.events.MovementType;
import com.traffic.simulation.events.SimulationEvent;
import com.traffic.simulation.events.StrategyControllerEvent;
import com.traffic.simulation.events.TrafficLightChangeEvent;
import com.traffic.simulation.events.VehicleArrivalEvent;
import com.traffic.simulation.events.VehicleDepartureEvent;
import com.traffic.simulation.scheduler.EventScheduler;
import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.EmergencyVehicle;
import com.traffic.traffic.vehicle.Vehicle;

import com.traffic.simulation.engine.SimulationTerminationStatus;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class TrafficBenchmarkRunner {

    public static BenchmarkResult run(
            BenchmarkScenario scenario,
            SignalControlStrategy strategy,
            String strategyName,
            int runIndex) {

        if (scenario == null) {
            throw new IllegalArgumentException("Scenario cannot be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }

        Intersection intersection = new Intersection("INT-BENCH");
        SimulationEngine engine = new SimulationEngine(intersection);
        SimulationContext context = engine.getContext();

        // 1. Initialize intersection: 4 approach roads and lanes
        for (Direction direction : Direction.values()) {
            context.addTrafficSignal(new TrafficSignal(direction));
            Road road = new Road("ROAD-" + direction, direction);
            Lane lane = new Lane("LANE-" + direction, MovementType.STRAIGHT);
            road.addLane(lane);
            intersection.addRoad(road);
        }

        // 2. Schedule vehicle arrivals
        Map<String, VehicleArrivalDescriptor> descriptorMap = new HashMap<>();
        Map<String, Vehicle> instantiatedVehicles = new HashMap<>();

        for (VehicleArrivalDescriptor desc : scenario.getArrivals()) {
            Vehicle vehicle = desc.instantiateVehicle();
            descriptorMap.put(vehicle.getId(), desc);
            instantiatedVehicles.put(vehicle.getId(), vehicle);

            engine.getScheduler().scheduleEvent(
                    new VehicleArrivalEvent(
                            vehicle,
                            "ROAD-" + desc.getDirection(),
                            desc.getArrivalTime(),
                            1
                    )
            );
        }

        // 3. Schedule StrategyControllerEvent
        context.scheduleEvent(
                new StrategyControllerEvent(
                        strategy,
                        scenario.getMaxPhases(),
                        0.0,
                        2
                )
        );

        // 4. Execute discrete-event simulation with metrics interception
        boolean[] starvationThresholdEverReached = new boolean[]{false};
        engine.addEventListener((event, ctx) -> {
            if (event instanceof StrategyControllerEvent) {
                double curTime = ctx.getClock().getCurrentTime();
                for (DirectionTraffic dt : ctx.getIntersection().getCurrentTrafficState(curTime).values()) {
                    if (dt.getMaxWaitingTime() >= 120.0 || dt.getWaitingTime() >= 120.0) {
                        starvationThresholdEverReached[0] = true;
                    }
                }
            }
        });

        long wallClockStart = System.nanoTime();
        SimulationTerminationStatus terminationStatus = engine.run();
        long wallClockEnd = System.nanoTime();
        double wallClockTimeMs = (wallClockEnd - wallClockStart) / 1_000_000.0;
        double totalSimulatedTime = engine.getClock().getCurrentTime();

        // 5. Post-simulation accounting and metric extraction
        int totalIntroduced = scenario.getVehicleCount();
        int completedCount = context.getCompletedVehicleCount();
        int queuedCount = intersection.getVehicleCount();
        int inTransitCount = context.getPendingDepartureCount();
        int remainingCount = queuedCount + inTransitCount;
        int pendingArrivalCount = context.getPendingArrivalCount();
        double completionRate = totalIntroduced > 0 ? ((double) completedCount / totalIntroduced) : 1.0;

        List<VehicleMetricRecord> vehicleRecords = new ArrayList<>();
        List<Double> completedTimesInSystem = new ArrayList<>();
        List<Double> emergencyTimesInSystem = new ArrayList<>();
        List<Double> emergencyGrantDelays = new ArrayList<>();
        List<Double> emergencyServiceDelays = new ArrayList<>();
        List<Double> completedWaitingTimes = new ArrayList<>();
        List<Double> unfinishedWaits = new ArrayList<>();
        List<Double> internalWaitingTimes = new ArrayList<>();

        // Process completed vehicles
        for (Vehicle completedVehicle : context.getCompletedVehicles()) {
            String vid = completedVehicle.getId();
            VehicleArrivalDescriptor desc = descriptorMap.get(vid);
            Double departureTime = context.getDepartureTime(vid);
            double arrivalTime = desc != null ? desc.getArrivalTime() : completedVehicle.getArrivalTime();
            double timeInSystem = (departureTime != null) ? (departureTime - arrivalTime) : 0.0;
            double queueWait = completedVehicle.getWaitingTime();

            completedTimesInSystem.add(timeInSystem);
            completedWaitingTimes.add(queueWait);
            internalWaitingTimes.add(queueWait);

            boolean isEmergency = completedVehicle instanceof EmergencyVehicle;
            Double emergencyGrantDelay = null;
            Double emergencyServiceDelay = null;

            if (isEmergency) {
                emergencyTimesInSystem.add(timeInSystem);

                Direction dir = desc != null ? desc.getDirection() : null;
                if (dir != null) {
                    OptionalDouble grantDelayOpt = context.getEmergencyGrantDelay(dir, arrivalTime);
                    if (grantDelayOpt.isPresent()) {
                        emergencyGrantDelay = grantDelayOpt.getAsDouble();
                        emergencyGrantDelays.add(emergencyGrantDelay);
                    }
                }

                Double movementTime = context.getMovementTime(vid);
                if (movementTime != null) {
                    emergencyServiceDelay = movementTime - arrivalTime;
                    emergencyServiceDelays.add(emergencyServiceDelay);
                }
            }

            vehicleRecords.add(new VehicleMetricRecord(
                    vid,
                    desc != null ? desc.getVehicleType().name() : (isEmergency ? "EMERGENCY" : "CAR"),
                    desc != null ? desc.getDirection() : Direction.NORTH,
                    arrivalTime,
                    departureTime,
                    timeInSystem,
                    queueWait,
                    true,
                    isEmergency,
                    emergencyGrantDelay,
                    emergencyServiceDelay,
                    null
            ));
        }

        // Process remaining vehicles in lanes (queued)
        for (Road road : intersection.getRoads()) {
            for (Lane lane : road.getLanes()) {
                for (Vehicle remainingVehicle : lane.getVehicles()) {
                    String vid = remainingVehicle.getId();
                    VehicleArrivalDescriptor desc = descriptorMap.get(vid);
                    double arrivalTime = desc != null ? desc.getArrivalTime() : remainingVehicle.getArrivalTime();
                    double timeInSystemSoFar = Math.max(0.0, totalSimulatedTime - arrivalTime);
                    double waitSoFar = remainingVehicle.getWaitingTime(totalSimulatedTime);

                    unfinishedWaits.add(waitSoFar);
                    internalWaitingTimes.add(remainingVehicle.getWaitingTime());

                    boolean isEmergency = remainingVehicle instanceof EmergencyVehicle;
                    Double emergencyGrantDelay = null;
                    if (isEmergency) {
                        Direction dir = desc != null ? desc.getDirection() : road.getDirection();
                        if (dir != null) {
                            OptionalDouble grantDelayOpt = context.getEmergencyGrantDelay(dir, arrivalTime);
                            if (grantDelayOpt.isPresent()) {
                                emergencyGrantDelay = grantDelayOpt.getAsDouble();
                                emergencyGrantDelays.add(emergencyGrantDelay);
                            }
                        }
                    }

                    vehicleRecords.add(new VehicleMetricRecord(
                            vid,
                            desc != null ? desc.getVehicleType().name() : (isEmergency ? "EMERGENCY" : "CAR"),
                            desc != null ? desc.getDirection() : (road.getDirection() != null ? road.getDirection() : Direction.NORTH),
                            arrivalTime,
                            null,
                            timeInSystemSoFar,
                            remainingVehicle.getWaitingTime(),
                            false,
                            isEmergency,
                            emergencyGrantDelay,
                            null,
                            waitSoFar
                    ));
                }
            }
        }

        // Process in-transit vehicles awaiting departure event
        for (VehicleDepartureEvent depEvent : context.getPendingDepartureEvents()) {
            Vehicle inTransitVehicle = depEvent.getVehicle();
            if (inTransitVehicle == null) {
                continue;
            }
            String vid = inTransitVehicle.getId();
            VehicleArrivalDescriptor desc = descriptorMap.get(vid);
            double arrivalTime = desc != null ? desc.getArrivalTime() : inTransitVehicle.getArrivalTime();
            double timeInSystemSoFar = Math.max(0.0, totalSimulatedTime - arrivalTime);
            double waitSoFar = inTransitVehicle.getWaitingTime();

            unfinishedWaits.add(waitSoFar);
            internalWaitingTimes.add(waitSoFar);

            boolean isEmergency = inTransitVehicle instanceof EmergencyVehicle;
            Double emergencyGrantDelay = null;
            Double emergencyServiceDelay = null;

            if (isEmergency) {
                Direction dir = desc != null ? desc.getDirection() : null;
                if (dir != null) {
                    OptionalDouble grantDelayOpt = context.getEmergencyGrantDelay(dir, arrivalTime);
                    if (grantDelayOpt.isPresent()) {
                        emergencyGrantDelay = grantDelayOpt.getAsDouble();
                        emergencyGrantDelays.add(emergencyGrantDelay);
                    }
                }

                Double movementTime = context.getMovementTime(vid);
                if (movementTime != null) {
                    emergencyServiceDelay = movementTime - arrivalTime;
                    emergencyServiceDelays.add(emergencyServiceDelay);
                }
            }

            vehicleRecords.add(new VehicleMetricRecord(
                    vid,
                    desc != null ? desc.getVehicleType().name() : (isEmergency ? "EMERGENCY" : "CAR"),
                    desc != null ? desc.getDirection() : Direction.NORTH,
                    arrivalTime,
                    null,
                    timeInSystemSoFar,
                    waitSoFar,
                    false,
                    isEmergency,
                    emergencyGrantDelay,
                    emergencyServiceDelay,
                    waitSoFar
            ));
        }

        // 6. Metric aggregations
        double throughput = (totalSimulatedTime > 0.0)
                ? (completedCount / totalSimulatedTime)
                : 0.0;

        double avgTimeInSystem = completedTimesInSystem.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double maxTimeInSystem = completedTimesInSystem.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        double minTimeInSystem = completedTimesInSystem.stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0);

        double avgCompletedWaitingTime = completedWaitingTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double maxCompletedWaitingTime = completedWaitingTimes.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        double avgUnfinishedWaitSoFar = unfinishedWaits.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double maxUnfinishedWaitSoFar = unfinishedWaits.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        double avgEmergencyTimeInSystem = emergencyTimesInSystem.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double maxEmergencyTimeInSystem = emergencyTimesInSystem.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        double avgEmergencyGrantDelay = emergencyGrantDelays.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double avgEmergencyServiceDelay = emergencyServiceDelays.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double emergencyResponseTimeToService = !emergencyServiceDelays.isEmpty()
                ? avgEmergencyServiceDelay
                : avgEmergencyGrantDelay;

        double reportedInternalAvgWaitingTime = internalWaitingTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double reportedInternalMaxWaitingTime = internalWaitingTimes.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        return new BenchmarkResult(
                scenario.getName(),
                strategyName,
                runIndex,
                totalIntroduced,
                completedCount,
                queuedCount,
                inTransitCount,
                remainingCount,
                pendingArrivalCount,
                completionRate,
                totalSimulatedTime,
                wallClockTimeMs,
                throughput,
                avgTimeInSystem,
                maxTimeInSystem,
                minTimeInSystem,
                avgCompletedWaitingTime,
                maxCompletedWaitingTime,
                avgUnfinishedWaitSoFar,
                maxUnfinishedWaitSoFar,
                avgEmergencyTimeInSystem,
                maxEmergencyTimeInSystem,
                avgEmergencyGrantDelay,
                avgEmergencyServiceDelay,
                emergencyResponseTimeToService,
                reportedInternalAvgWaitingTime,
                reportedInternalMaxWaitingTime,
                starvationThresholdEverReached[0],
                terminationStatus,
                vehicleRecords
        );
    }
}

