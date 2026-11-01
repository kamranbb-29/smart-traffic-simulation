package com.traffic.simulation.engine;

/**
 * Represents a time interval during which a traffic signal was GREEN for a given approach.
 */
public class GreenInterval {

    private final double startTime;
    private double endTime;

    public GreenInterval(double startTime) {
        if (!Double.isFinite(startTime) || startTime < 0) {
            throw new IllegalArgumentException("Start time must be finite and non-negative");
        }
        this.startTime = startTime;
        this.endTime = Double.POSITIVE_INFINITY;
    }

    public void close(double endTime) {
        if (!Double.isFinite(endTime) || endTime < startTime) {
            throw new IllegalArgumentException("End time must be finite and >= start time");
        }
        this.endTime = endTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public boolean isOpen() {
        return Double.isInfinite(endTime);
    }

    /**
     * Checks if this interval was actively GREEN at the specified timestamp.
     * Uses half-open interval semantics: start time is inclusive, closed end time is exclusive.
     * An open interval remains active from its start time onward.
     */
    public boolean isActiveAt(double timestamp) {
        if (isOpen()) {
            return timestamp >= startTime;
        }
        return timestamp >= startTime && timestamp < endTime;
    }

    /**
     * Checks if this interval started strictly after the specified timestamp.
     */
    public boolean startsAfter(double timestamp) {
        return startTime > timestamp;
    }

    /**
     * Checks if this interval ended strictly before the specified timestamp.
     */
    public boolean endedBefore(double timestamp) {
        return endTime < timestamp;
    }
}

