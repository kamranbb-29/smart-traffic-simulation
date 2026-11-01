
package com.traffic.statistics;

import java.util.List;

public class StrategyComparisonReporter {

    public void displayComparison(List<StrategyResult> results) {

        if (results == null || results.isEmpty()) {
            System.out.println("No strategy results available.");
            return;
        }

        System.out.println("\n========== STRATEGY COMPARISON ==========");

        for (StrategyResult result : results) {

            System.out.println("\nStrategy: "
                    + result.getStrategyName());

            System.out.println("Completed Vehicles: "
                    + result.getCompletedVehicles());

            System.out.println("Average Waiting Time: "
                    + result.getAverageWaitingTime());

            System.out.println("Maximum Waiting Time: "
                    + result.getMaximumWaitingTime());

            System.out.println("Throughput: "
                    + result.getThroughput());

            System.out.println("Average Queue Length: "
                    + result.getAverageQueueLength());
        }

        System.out.println("\n=========================================");
    }
}
