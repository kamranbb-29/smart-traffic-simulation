package com.traffic.benchmark;

import java.util.Collections;
import java.util.List;

public class BenchmarkScenario {

    private final String name;
    private final String description;
    private final int maxPhases;
    private final List<VehicleArrivalDescriptor> arrivals;

    public BenchmarkScenario(
            String name,
            String description,
            int maxPhases,
            List<VehicleArrivalDescriptor> arrivals) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Scenario name cannot be empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (maxPhases <= 0) {
            throw new IllegalArgumentException("maxPhases must be positive");
        }
        if (arrivals == null) {
            throw new IllegalArgumentException("Arrivals list cannot be null");
        }

        this.name = name;
        this.description = description;
        this.maxPhases = maxPhases;
        this.arrivals = List.copyOf(arrivals);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxPhases() {
        return maxPhases;
    }

    public List<VehicleArrivalDescriptor> getArrivals() {
        return arrivals;
    }

    public int getVehicleCount() {
        return arrivals.size();
    }
}

