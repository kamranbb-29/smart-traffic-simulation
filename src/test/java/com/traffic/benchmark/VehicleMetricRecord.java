package com.traffic.benchmark;

import com.traffic.control.Direction;

public class VehicleMetricRecord {

    private final String id;
    private final String vehicleType;
    private final Direction direction;
    private final double arrivalTime;
    private final Double departureTime;
    private final double timeInSystem;
    private final double reportedInternalWaitingTime;
    private final boolean completed;
    private final boolean emergency;
    private final Double emergencyGrantDelay;
    private final Double emergencyServiceDelay;
    private final Double unfinishedWaitSoFar;

    public VehicleMetricRecord(
            String id,
            String vehicleType,
            Direction direction,
            double arrivalTime,
            Double departureTime,
            double timeInSystem,
            double reportedInternalWaitingTime,
            boolean completed,
            boolean emergency) {

        this(id, vehicleType, direction, arrivalTime, departureTime, timeInSystem,
                reportedInternalWaitingTime, completed, emergency, null, null, null);
    }

    public VehicleMetricRecord(
            String id,
            String vehicleType,
            Direction direction,
            double arrivalTime,
            Double departureTime,
            double timeInSystem,
            double reportedInternalWaitingTime,
            boolean completed,
            boolean emergency,
            Double emergencyGrantDelay,
            Double emergencyServiceDelay,
            Double unfinishedWaitSoFar) {

        this.id = id;
        this.vehicleType = vehicleType;
        this.direction = direction;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.timeInSystem = timeInSystem;
        this.reportedInternalWaitingTime = reportedInternalWaitingTime;
        this.completed = completed;
        this.emergency = emergency;
        this.emergencyGrantDelay = emergencyGrantDelay;
        this.emergencyServiceDelay = emergencyServiceDelay;
        this.unfinishedWaitSoFar = unfinishedWaitSoFar;
    }

    public String getId() {
        return id;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public Double getDepartureTime() {
        return departureTime;
    }

    public double getTimeInSystem() {
        return timeInSystem;
    }

    public double getReportedInternalWaitingTime() {
        return reportedInternalWaitingTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public Double getEmergencyGrantDelay() {
        return emergencyGrantDelay;
    }

    public Double getEmergencyServiceDelay() {
        return emergencyServiceDelay;
    }

    public Double getUnfinishedWaitSoFar() {
        return unfinishedWaitSoFar;
    }
}

