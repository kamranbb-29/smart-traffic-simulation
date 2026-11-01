package com.traffic.benchmark;

import com.traffic.control.Direction;
import com.traffic.simulation.events.MovementType;
import com.traffic.benchmark.VehicleArrivalDescriptor.VehicleType;

import java.util.ArrayList;
import java.util.List;

public class ScenarioFactory {

    public static BenchmarkScenario createLightTrafficScenario() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();
        arrivals.add(new VehicleArrivalDescriptor("LT-N1", VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, 1.0));
        arrivals.add(new VehicleArrivalDescriptor("LT-N2", VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, 4.0));
        arrivals.add(new VehicleArrivalDescriptor("LT-E1", VehicleType.CAR, Direction.EAST, MovementType.STRAIGHT, 6.0));
        arrivals.add(new VehicleArrivalDescriptor("LT-E2", VehicleType.CAR, Direction.EAST, MovementType.STRAIGHT, 9.0));
        arrivals.add(new VehicleArrivalDescriptor("LT-S1", VehicleType.CAR, Direction.SOUTH, MovementType.STRAIGHT, 11.0));
        arrivals.add(new VehicleArrivalDescriptor("LT-S2", VehicleType.CAR, Direction.SOUTH, MovementType.STRAIGHT, 14.0));
        arrivals.add(new VehicleArrivalDescriptor("LT-W1", VehicleType.CAR, Direction.WEST, MovementType.STRAIGHT, 16.0));
        arrivals.add(new VehicleArrivalDescriptor("LT-W2", VehicleType.CAR, Direction.WEST, MovementType.STRAIGHT, 19.0));

        return new BenchmarkScenario(
                "Light Traffic",
                "Small number of vehicles (8 total, 2 per direction) distributed across all approaches with staggered arrivals.",
                8,
                arrivals
        );
    }

    public static BenchmarkScenario createBalancedTrafficScenario() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();
        double[] times = {2.0, 6.0, 10.0, 14.0, 18.0, 22.0};
        int id = 1;

        for (Direction dir : Direction.values()) {
            for (double t : times) {
                arrivals.add(new VehicleArrivalDescriptor(
                        "BAL-" + dir.name().charAt(0) + (id++),
                        VehicleType.CAR,
                        dir,
                        MovementType.STRAIGHT,
                        t
                ));
            }
        }

        return new BenchmarkScenario(
                "Balanced Traffic",
                "Equal distribution of vehicles across all 4 directions (6 vehicles per approach, 24 total) arriving concurrently.",
                12,
                arrivals
        );
    }

    public static BenchmarkScenario createUnequalTrafficScenario() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();

        // NORTH has 24 vehicles arriving from t=1.0 to t=35.5 (every 1.5s)
        for (int i = 1; i <= 24; i++) {
            arrivals.add(new VehicleArrivalDescriptor(
                    "UNEQ-N" + i,
                    VehicleType.CAR,
                    Direction.NORTH,
                    MovementType.STRAIGHT,
                    1.0 + (i - 1) * 1.5
            ));
        }

        // EAST, SOUTH, WEST have 2 vehicles each
        arrivals.add(new VehicleArrivalDescriptor("UNEQ-E1", VehicleType.CAR, Direction.EAST, MovementType.STRAIGHT, 5.0));
        arrivals.add(new VehicleArrivalDescriptor("UNEQ-E2", VehicleType.CAR, Direction.EAST, MovementType.STRAIGHT, 20.0));

        arrivals.add(new VehicleArrivalDescriptor("UNEQ-S1", VehicleType.CAR, Direction.SOUTH, MovementType.STRAIGHT, 10.0));
        arrivals.add(new VehicleArrivalDescriptor("UNEQ-S2", VehicleType.CAR, Direction.SOUTH, MovementType.STRAIGHT, 25.0));

        arrivals.add(new VehicleArrivalDescriptor("UNEQ-W1", VehicleType.CAR, Direction.WEST, MovementType.STRAIGHT, 15.0));
        arrivals.add(new VehicleArrivalDescriptor("UNEQ-W2", VehicleType.CAR, Direction.WEST, MovementType.STRAIGHT, 30.0));

        return new BenchmarkScenario(
                "Unequal Traffic",
                "Asymmetric demand: NORTH is heavily loaded (24 vehicles) while EAST, SOUTH, and WEST are sparse (2 vehicles each, 30 total).",
                10,
                arrivals
        );
    }

    public static BenchmarkScenario createHeavyTrafficScenario() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();
        int id = 1;

        // 16 vehicles per approach = 64 vehicles total
        for (Direction dir : Direction.values()) {
            for (int i = 0; i < 16; i++) {
                double arrivalTime = 1.0 + i * 3.5;
                arrivals.add(new VehicleArrivalDescriptor(
                        "HVY-" + dir.name().charAt(0) + (id++),
                        VehicleType.CAR,
                        dir,
                        MovementType.STRAIGHT,
                        arrivalTime
                ));
            }
        }

        return new BenchmarkScenario(
                "Heavy Traffic",
                "Sustained heavy traffic (64 vehicles, 16 per approach) testing multi-vehicle queueing, headway (1.0s), and continuous throughput.",
                16,
                arrivals
        );
    }

    public static BenchmarkScenario createEmergencyTrafficScenario() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();

        // Normal background traffic (4 per approach = 16 cars)
        for (Direction dir : Direction.values()) {
            for (int i = 1; i <= 4; i++) {
                arrivals.add(new VehicleArrivalDescriptor(
                        "NORM-" + dir.name().charAt(0) + i,
                        VehicleType.CAR,
                        dir,
                        MovementType.STRAIGHT,
                        2.0 + (i - 1) * 7.0
                ));
            }
        }

        // Competing emergency vehicles
        arrivals.add(new VehicleArrivalDescriptor("EMG-AMB-NORTH", VehicleType.EMERGENCY, Direction.NORTH, MovementType.STRAIGHT, 12.0));
        arrivals.add(new VehicleArrivalDescriptor("EMG-FIRE-EAST", VehicleType.EMERGENCY, Direction.EAST, MovementType.STRAIGHT, 14.0));
        arrivals.add(new VehicleArrivalDescriptor("EMG-AMB-SOUTH", VehicleType.EMERGENCY, Direction.SOUTH, MovementType.STRAIGHT, 22.0));
        arrivals.add(new VehicleArrivalDescriptor("EMG-POLICE-WEST", VehicleType.EMERGENCY, Direction.WEST, MovementType.STRAIGHT, 26.0));

        return new BenchmarkScenario(
                "Emergency Traffic",
                "Four competing emergency vehicles arriving across different approaches amidst normal background traffic (20 vehicles total).",
                12,
                arrivals
        );
    }

    public static BenchmarkScenario createStarvationScenario() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();

        // Continuous high influx on NORTH: 2 vehicles arriving every 5s from t=0.0 to t=130.0 (54 vehicles on NORTH)
        for (int i = 0; i < 27; i++) {
            double baseTime = i * 5.0;
            arrivals.add(new VehicleArrivalDescriptor("STARV-N" + (i * 2 + 1), VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, baseTime));
            arrivals.add(new VehicleArrivalDescriptor("STARV-N" + (i * 2 + 2), VehicleType.CAR, Direction.NORTH, MovementType.STRAIGHT, baseTime + 1.0));
        }

        // Competing low-density approaches arriving early (t=2.0s)
        arrivals.add(new VehicleArrivalDescriptor("STARV-S1", VehicleType.CAR, Direction.SOUTH, MovementType.STRAIGHT, 2.0));
        arrivals.add(new VehicleArrivalDescriptor("STARV-S2", VehicleType.CAR, Direction.SOUTH, MovementType.STRAIGHT, 3.0));

        arrivals.add(new VehicleArrivalDescriptor("STARV-E1", VehicleType.CAR, Direction.EAST, MovementType.STRAIGHT, 2.0));
        arrivals.add(new VehicleArrivalDescriptor("STARV-E2", VehicleType.CAR, Direction.EAST, MovementType.STRAIGHT, 3.0));

        arrivals.add(new VehicleArrivalDescriptor("STARV-W1", VehicleType.CAR, Direction.WEST, MovementType.STRAIGHT, 2.0));
        arrivals.add(new VehicleArrivalDescriptor("STARV-W2", VehicleType.CAR, Direction.WEST, MovementType.STRAIGHT, 3.0));

        return new BenchmarkScenario(
                "Starvation Test",
                "High continuous traffic on NORTH keeping density elevated for >120s while SOUTH, EAST, and WEST wait, testing the adaptive 120s starvation threshold.",
                16, // 16 phases = 160.0 simulation seconds
                arrivals
        );
    }

    public static BenchmarkScenario createMixedTrafficScenario() {
        List<VehicleArrivalDescriptor> arrivals = new ArrayList<>();

        // Mixed vehicle types: Car, Bus, Bike, EmergencyVehicle across all 4 directions
        Direction[] dirs = Direction.values();
        int count = 1;

        for (int i = 0; i < dirs.length; i++) {
            Direction dir = dirs[i];

            // 1. Cars
            arrivals.add(new VehicleArrivalDescriptor("MIX-CAR-" + count++, VehicleType.CAR, dir, MovementType.STRAIGHT, 1.0 + i * 2.0));
            arrivals.add(new VehicleArrivalDescriptor("MIX-CAR-" + count++, VehicleType.CAR, dir, MovementType.STRAIGHT, 2.0 + i * 2.0)); // Queue behind first car
            arrivals.add(new VehicleArrivalDescriptor("MIX-CAR-" + count++, VehicleType.CAR, dir, MovementType.STRAIGHT, 8.0 + i * 3.0));

            // 2. Bus
            arrivals.add(new VehicleArrivalDescriptor("MIX-BUS-" + count++, VehicleType.BUS, dir, MovementType.STRAIGHT, 15.0 + i * 4.0));

            // 3. Bike
            arrivals.add(new VehicleArrivalDescriptor("MIX-BIKE-" + count++, VehicleType.BIKE, dir, MovementType.STRAIGHT, 4.0 + i * 3.0));
            arrivals.add(new VehicleArrivalDescriptor("MIX-BIKE-" + count++, VehicleType.BIKE, dir, MovementType.STRAIGHT, 22.0 + i * 2.0));

            // 4. Additional cars
            arrivals.add(new VehicleArrivalDescriptor("MIX-CAR-" + count++, VehicleType.CAR, dir, MovementType.STRAIGHT, 30.0 + i * 2.0));
            arrivals.add(new VehicleArrivalDescriptor("MIX-CAR-" + count++, VehicleType.CAR, dir, MovementType.STRAIGHT, 31.0 + i * 2.0));
            arrivals.add(new VehicleArrivalDescriptor("MIX-CAR-" + count++, VehicleType.CAR, dir, MovementType.STRAIGHT, 40.0 + i * 2.0));
        }

        // Add 2 emergency vehicles
        arrivals.add(new VehicleArrivalDescriptor("MIX-EMG-AMB", VehicleType.EMERGENCY, Direction.NORTH, MovementType.STRAIGHT, 16.0));
        arrivals.add(new VehicleArrivalDescriptor("MIX-EMG-FIRE", VehicleType.EMERGENCY, Direction.EAST, MovementType.STRAIGHT, 28.0));

        return new BenchmarkScenario(
                "Mixed Traffic",
                "Heterogeneous vehicle classes (Car, Bus, Bike, Emergency) with burst arrivals in the same lane testing FIFO headway and priority.",
                14,
                arrivals
        );
    }

    public static List<BenchmarkScenario> getAllScenarios() {
        return List.of(
                createLightTrafficScenario(),
                createBalancedTrafficScenario(),
                createUnequalTrafficScenario(),
                createHeavyTrafficScenario(),
                createEmergencyTrafficScenario(),
                createStarvationScenario(),
                createMixedTrafficScenario()
        );
    }
}

