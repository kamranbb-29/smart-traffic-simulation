
package com.traffic.app;

import com.traffic.control.AdaptivePriorityStrategy;
import com.traffic.control.DensityBasedStrategy;
import com.traffic.control.Direction;
import com.traffic.control.FixedTimeStrategy;
import com.traffic.control.SignalControlStrategy;
import com.traffic.control.TrafficSignal;

import com.traffic.simulation.engine.SimulationContext;
import com.traffic.simulation.engine.SimulationEngine;
import com.traffic.simulation.events.MovementType;
import com.traffic.simulation.events.StrategyControllerEvent;
import com.traffic.simulation.events.VehicleArrivalEvent;

import com.traffic.traffic.intersection.Intersection;
import com.traffic.traffic.road.Lane;
import com.traffic.traffic.road.Road;
import com.traffic.traffic.vehicle.Car;
import com.traffic.traffic.vehicle.EmergencyVehicle;
import com.traffic.traffic.vehicle.Vehicle;

import java.util.Scanner;

public class SimulationSetup {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            System.out.println("\n======================================");
            System.out.println("       SMART TRAFFIC SIMULATION");
            System.out.println("======================================");

            System.out.println("1. Fixed-Time Strategy");
            System.out.println("2. Density-Based Strategy");
            System.out.println("3. Adaptive-Priority Strategy");
            System.out.println("4. Exit");

            System.out.print("\nChoose a strategy: ");

            String choice = SCANNER.nextLine().trim();

            switch (choice) {

                case "1" -> runSimulation(
                        new FixedTimeStrategy(),
                        "FIXED-TIME");

                case "2" -> runSimulation(
                        new DensityBasedStrategy(),
                        "DENSITY-BASED");

                case "3" -> runSimulation(
                        new AdaptivePriorityStrategy(),
                        "ADAPTIVE-PRIORITY");

                case "4" -> {
                    running = false;
                    System.out.println("Exiting simulation.");
                }

                default -> System.out.println("Invalid option.");
            }
        }

        SCANNER.close();
    }

    private static void runSimulation(
            SignalControlStrategy strategy,
            String strategyName) {

        System.out.println("\n======================================");
        System.out.println("Selected Strategy: " + strategyName);
        System.out.println("======================================");

        Intersection intersection = new Intersection("INT1");

        SimulationEngine engine = new SimulationEngine(intersection);

        SimulationContext context = engine.getContext();

        // Create roads, lanes and traffic signals.
        for (Direction direction : Direction.values()) {

            context.addTrafficSignal(new TrafficSignal(direction));

            Road road = new Road(
                    roadId(direction),
                    direction);

            Lane lane = new Lane(
                    laneId(direction),
                    MovementType.STRAIGHT);

            road.addLane(lane);
            intersection.addRoad(road);
        }

        System.out.println("\nIntersection initialized.");
        System.out.println("Roads created: " + intersection.getRoadCount());

        // Schedule vehicle arrivals.
        scheduleVehicle(
                engine,
                new Car("CAR-1", 1.0),
                Direction.NORTH,
                1.0);

        scheduleVehicle(
                engine,
                new Car("CAR-2", 11.0),
                Direction.SOUTH,
                11.0);

        scheduleVehicle(
                engine,
                new EmergencyVehicle("AMB-1", 21.0),
                Direction.EAST,
                21.0);

        scheduleVehicle(
                engine,
                new Car("CAR-3", 31.0),
                Direction.WEST,
                31.0);

        // Start the strategy controller.
        // It makes eight decisions, one every ten simulation seconds.
        context.scheduleEvent(new StrategyControllerEvent(
                strategy,
                8,
                0.0,
                1));

        System.out.println("\nStarting simulation...\n");

        engine.run();

        // Display final statistics.
        System.out.println("\n========== FINAL STATISTICS ==========");

        System.out.println("Strategy: " + strategyName);
        System.out.println("Intersection: " + intersection.getId());
        System.out.println("Roads: " + intersection.getRoadCount());

        System.out.println(
                "Vehicles remaining: " + intersection.getVehicleCount());

        System.out.println(
                "Completed vehicles: " + context.getCompletedVehicleCount());

        System.out.println(
                "Simulation time: " + engine.getClock().getCurrentTime());

        System.out.println("======================================");
    }

    private static void scheduleVehicle(
            SimulationEngine engine,
            Vehicle vehicle,
            Direction direction,
            double arrivalTime) {

        engine.getScheduler().scheduleEvent(
                new VehicleArrivalEvent(
                        vehicle,
                        roadId(direction),
                        arrivalTime,
                        2));
    }

    private static String roadId(Direction direction) {
        return "ROAD-" + direction;
    }

    private static String laneId(Direction direction) {
        return "LANE-" + direction;
    }
}
