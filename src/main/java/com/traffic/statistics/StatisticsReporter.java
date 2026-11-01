
package com.traffic.statistics;

public class StatisticsReporter {

    public void displayReport(TrafficStatistics stats) {

        System.out.println("===== TRAFFIC SIMULATION REPORT =====");

        System.out.println("Completed Vehicles: "
                + stats.getCompletedVehicles());

        System.out.println("Average Waiting Time: "
                + stats.getAverageWaitingTime() + " seconds");

        System.out.println("Maximum Waiting Time: "
                + stats.getMaximumWaitingTime() + " seconds");

        System.out.println("Throughput: "
                + stats.getThroughput() + " vehicles/second");

        System.out.println("Average Queue Length: "
                + stats.getAverageQueueLength() + " vehicles");

        System.out.println("=====================================");
    }
}
