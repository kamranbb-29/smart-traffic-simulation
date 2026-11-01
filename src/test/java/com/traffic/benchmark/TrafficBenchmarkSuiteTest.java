package com.traffic.benchmark;

import com.traffic.control.AdaptivePriorityStrategy;
import com.traffic.control.DensityBasedStrategy;
import com.traffic.control.FixedTimeStrategy;
import com.traffic.control.SignalControlStrategy;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrafficBenchmarkSuiteTest {

    private record StrategyConfig(String name, Supplier<SignalControlStrategy> factory) {}

    @Test
    void runFullBenchmarkSuiteAndGenerateReports() throws IOException {
        List<BenchmarkScenario> scenarios = ScenarioFactory.getAllScenarios();

        List<StrategyConfig> strategies = List.of(
                new StrategyConfig("FIXED-TIME", FixedTimeStrategy::new),
                new StrategyConfig("DENSITY-BASED", DensityBasedStrategy::new),
                new StrategyConfig("ADAPTIVE-PRIORITY", AdaptivePriorityStrategy::new)
        );

        int runsPerCombination = 3;
        List<BenchmarkResult> allResults = new ArrayList<>();

        System.out.println("===============================================================================");
        System.out.println("         STARTING FULL SMART TRAFFIC SIMULATION BENCHMARK SUITE");
        System.out.println("===============================================================================");

        for (BenchmarkScenario scenario : scenarios) {
            System.out.println("\n-------------------------------------------------------------------------------");
            System.out.println("Scenario: " + scenario.getName() + " (" + scenario.getVehicleCount() + " vehicles, maxPhases=" + scenario.getMaxPhases() + ")");
            System.out.println("-------------------------------------------------------------------------------");

            for (StrategyConfig stratConfig : strategies) {
                for (int run = 1; run <= runsPerCombination; run++) {
                    SignalControlStrategy strategy = stratConfig.factory().get();
                    BenchmarkResult result = TrafficBenchmarkRunner.run(
                            scenario,
                            strategy,
                            stratConfig.name(),
                            run
                    );

                    // Strictly verify vehicle accounting
                    assertTrue(result.isAccountingValid(),
                            "Accounting invalid in " + scenario.getName() + " for " + stratConfig.name());

                    allResults.add(result);

                    if (run == 1) {
                        System.out.printf("  [%-17s] Completed: %2d/%2d | SimTime: %5.1fs | Tput: %5.3f veh/s | AvgTimeInSys: %5.2fs | IntWait: %.1fs | Status: %s%n",
                                stratConfig.name(),
                                result.getCompletedCount(),
                                result.getTotalIntroduced(),
                                result.getTotalSimulatedTime(),
                                result.getThroughput(),
                                result.getAvgTimeInSystem(),
                                result.getReportedInternalAvgWaitingTime(),
                                result.isSimulationComplete() ? "COMPLETE" : "TRUNCATED"
                        );
                    }
                }
            }
        }

        System.out.println("\n===============================================================================");
        System.out.println("All benchmark runs completed successfully! Generating output artifacts...");
        System.out.println("===============================================================================");

        String csvPath = "test-results/benchmark-results.csv";
        String reportPath = "reports/benchmark-report.md";
        String chartsDir = "reports/charts";

        BenchmarkReportGenerator.generateAll(allResults, csvPath, reportPath, chartsDir);

        assertTrue(Files.exists(Paths.get(csvPath)), "CSV file must exist");
        assertTrue(Files.size(Paths.get(csvPath)) > 0, "CSV file must not be empty");

        assertTrue(Files.exists(Paths.get(reportPath)), "Markdown report must exist");
        assertTrue(Files.size(Paths.get(reportPath)) > 0, "Markdown report must not be empty");

        assertTrue(Files.exists(Paths.get(chartsDir, "avg_time_in_system.svg")), "Time in system SVG must exist");
        assertTrue(Files.exists(Paths.get(chartsDir, "throughput.svg")), "Throughput SVG must exist");

        System.out.println("Generated CSV: " + new File(csvPath).getAbsolutePath());
        System.out.println("Generated Report: " + new File(reportPath).getAbsolutePath());
        System.out.println("Generated Chart 1: " + new File(chartsDir, "avg_time_in_system.svg").getAbsolutePath());
        System.out.println("Generated Chart 2: " + new File(chartsDir, "throughput.svg").getAbsolutePath());
    }
}

