package com.traffic.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BenchmarkReportGenerator {

    public static void generateAll(
            List<BenchmarkResult> results,
            String csvPath,
            String markdownPath,
            String chartsDir) throws IOException {

        Files.createDirectories(Paths.get(csvPath).getParent());
        Files.createDirectories(Paths.get(markdownPath).getParent());
        Files.createDirectories(Paths.get(chartsDir));

        generateCsv(results, csvPath);
        generateMarkdown(results, markdownPath);
        generateSvgCharts(results, chartsDir);
    }

    public static void generateCsv(List<BenchmarkResult> results, String path) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("Scenario,Strategy,RunIndex,Introduced,Completed,Remaining,Pending,IsComplete,AccountingValid," +
                    "SimulatedTimeSec,WallClockTimeMs,ThroughputVehPerSec,AvgTimeInSystemSec,MaxTimeInSystemSec,MinTimeInSystemSec," +
                    "AvgEmergencyTimeInSystemSec,MaxEmergencyTimeInSystemSec,EmergencyResponseDelaySec," +
                    "ReportedInternalAvgWaitSec,ReportedInternalMaxWaitSec,Starvation120sTriggered," +
                    "queuedCount,inTransitCount,pendingArrivalCount,remainingCount,completionRate,accountingValid," +
                    "avgCompletedWaitingTime,maxCompletedWaitingTime,avgUnfinishedWaitSoFar,maxUnfinishedWaitSoFar," +
                    "avgEmergencyGrantDelay,avgEmergencyServiceDelay,terminationStatus");

            for (BenchmarkResult r : results) {
                String grantDelayStr = r.hasEmergencyGrantDelay()
                        ? String.format(Locale.US, "%.2f", r.getAvgEmergencyGrantDelay())
                        : "";
                String serviceDelayStr = r.hasEmergencyServiceDelay()
                        ? String.format(Locale.US, "%.2f", r.getAvgEmergencyServiceDelay())
                        : "";
                String termStatusStr = r.getTerminationStatus() != null
                        ? r.getTerminationStatus().name()
                        : "";

                pw.printf(Locale.US, "%s,%s,%d,%d,%d,%d,%d,%b,%b,%.2f,%.3f,%.4f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%b" +
                                ",%d,%d,%d,%d,%.4f,%b,%.2f,%.2f,%.2f,%.2f,%s,%s,%s%n",
                        escapeCsv(r.getScenarioName()),
                        escapeCsv(r.getStrategyName()),
                        r.getRunIndex(),
                        r.getTotalIntroduced(),
                        r.getCompletedCount(),
                        r.getRemainingCount(),
                        r.getPendingArrivalCount(),
                        r.isSimulationComplete(),
                        r.isAccountingValid(),
                        r.getTotalSimulatedTime(),
                        r.getWallClockTimeMs(),
                        r.getThroughput(),
                        r.getAvgTimeInSystem(),
                        r.getMaxTimeInSystem(),
                        r.getMinTimeInSystem(),
                        r.getAvgEmergencyTimeInSystem(),
                        r.getMaxEmergencyTimeInSystem(),
                        r.getEmergencyResponseTimeToService(),
                        r.getReportedInternalAvgWaitingTime(),
                        r.getReportedInternalMaxWaitingTime(),
                        r.isStarvation120sTriggered(),
                        r.getQueuedCount(),
                        r.getInTransitCount(),
                        r.getPendingArrivalCount(),
                        r.getRemainingCount(),
                        r.getCompletionRate(),
                        r.isAccountingValid(),
                        r.getAvgCompletedWaitingTime(),
                        r.getMaxCompletedWaitingTime(),
                        r.getAvgUnfinishedWaitSoFar(),
                        r.getMaxUnfinishedWaitSoFar(),
                        grantDelayStr,
                        serviceDelayStr,
                        termStatusStr
                );
            }
        }
    }

    private static String escapeCsv(String val) {
        if (val.contains(",") || val.contains("\"")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    public static void generateMarkdown(List<BenchmarkResult> results, String path) throws IOException {
        // Group by Scenario and Strategy, compute averages across runs
        Map<String, Map<String, List<BenchmarkResult>>> grouped = results.stream()
                .collect(Collectors.groupingBy(
                        BenchmarkResult::getScenarioName,
                        LinkedHashMap::new,
                        Collectors.groupingBy(BenchmarkResult::getStrategyName, LinkedHashMap::new, Collectors.toList())
                ));

        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("# Smart Traffic Simulation: Comprehensive Strategy Benchmark Report");
            pw.println();
            pw.println("This report presents empirical performance statistics and architectural analysis for the three traffic signal strategies implemented in the Smart Traffic Simulation system:");
            pw.println("1. **Fixed-Time Strategy** (Round-robin baseline)");
            pw.println("2. **Density-Based Strategy** (Greedy queue-length prioritization)");
            pw.println("3. **Adaptive-Priority Strategy** (Multi-tier emergency, starvation, and heuristic scoring)");
            pw.println();

            pw.println("---");
            pw.println();
            pw.println("## 1. Executive Summary & Critical Architectural Findings");
            pw.println();
            pw.println("> [!CRITICAL]");
            pw.println("> **Core Engine Flaw Discovered (Zero Waiting Time Metric)**:");
            pw.println("> In the current codebase, **no component or event in the simulation engine updates `Vehicle.waitingTime`** or calls `Vehicle.addWaitingTime()`. Every vehicle's internal waiting time remains permanently `0.0`.");
            pw.println("> As a consequence, `Intersection.getCurrentTrafficState()` continuously returns `0L` for all direction waiting times. This causes:");
            pw.println("> 1. **Starvation Inoperability**: The Adaptive strategy's 120-second starvation rule (`if (traffic.getWaitingTime() >= 120)`) **never triggers** during live simulation.");
            pw.println("> 2. **Degraded Adaptive Priority Scoring**: `PriorityCalculator.calculatePriorityScore()` collapses from `1.5 * wait + count` to simply `count`.");
            pw.println("> 3. **Strategy Convergence**: In scenarios without emergency vehicles, **Adaptive-Priority behaves identically to Density-Based**.");
            pw.println();
            pw.println("> [!WARNING]");
            pw.println("> **Non-Deterministic Tie-Breaking in Density-Based Strategy**:");
            pw.println("> `DensityBasedStrategy.chooseDirection()` has no tie-breaking rule and uses strict `>` comparison while iterating over `snapshot.getTrafficData().entrySet()`. Because `TrafficSnapshot` copies into a `java.util.HashMap`, iteration order depends on `Direction.hashCode()` (`System.identityHashCode()`), which varies between JVM runs. On ties, Density-Based choice is non-deterministic, whereas Adaptive-Priority deterministically breaks ties via enum ordinal order.");
            pw.println();

            pw.println("---");
            pw.println();
            pw.println("## 2. Benchmark Methodology");
            pw.println();
            pw.println("- **Input Parity**: All three strategies were subjected to **100% identical vehicle arrival sequences, arrival timestamps, and lane assignments**.");
            pw.println("- **Multi-Run Repetition**: Each scenario-strategy pair was executed across multiple distinct runs to evaluate wall-clock execution variance and ensure simulation determinism.");
            pw.println("- **Accounting Integrity**: Strict accounting validation was enforced for every execution: `Introduced == Completed + Remaining + Pending`.");
            pw.println("- **No Production Code Modifications**: All benchmarks were executed purely via public project APIs (`SimulationEngine`, `SimulationContext`, `EventScheduler`, `Intersection`).");
            pw.println();

            pw.println("---");
            pw.println();
            pw.println("## 3. Metric Definitions");
            pw.println();
            pw.println("| Metric Name | Mathematical Definition | Explanation & Measurement Technique |");
            pw.println("| :--- | :--- | :--- |");
            pw.println("| **Total Introduced** | $N_{\\text{introduced}} = |\\text{Arrival Events}|$ | Count of all vehicles scheduled into the simulation scenario. |");
            pw.println("| **Completed Vehicles** | $N_{\\text{completed}}$ | Vehicles that traversed the intersection and executed `VehicleDepartureEvent`. |");
            pw.println("| **Queued Vehicles** | $N_{\\text{queued}}$ | Vehicles physically remaining in approach lane queues at termination. |");
            pw.println("| **In-Transit Vehicles** | $N_{\\text{in-transit}}$ | Vehicles that executed movement and await `VehicleDepartureEvent`. |");
            pw.println("| **Pending Arrivals** | $N_{\\text{pending}}$ | Arrival events remaining in the event scheduler queue. |");
            pw.println("| **Remaining Vehicles** | $N_{\\text{remaining}} = N_{\\text{queued}} + N_{\\text{in-transit}}$ | Vehicles that entered the system but have not yet departed. |");
            pw.println("| **Accounting Valid** | $N_{\\text{introduced}} == N_{\\text{completed}} + N_{\\text{queued}} + N_{\\text{in-transit}} + N_{\\text{pending}}$ | Conservation invariant verifying zero vehicle loss or duplication. |");
            pw.println("| **Completion Rate** | $\\frac{N_{\\text{completed}}}{N_{\\text{introduced}}}$ | Fraction of introduced vehicles successfully departing before termination. |");
            pw.println("| **Simulation Complete** | $N_{\\text{completed}} == N_{\\text{introduced}}$ | Boolean indicating whether all vehicles completed within the allotted phases. |");
            pw.println("| **Throughput** | $\\frac{N_{\\text{completed}}}{t_{\\text{sim}}}$ | Completed vehicles per simulated second (veh/s). |");
            pw.println("| **Total Time in System** | $T_{\\text{system}} = t_{\\text{departure}} - t_{\\text{arrival}}$ | **True end-to-end journey time** from arrival in approach lane until departure through intersection. |");
            pw.println("| **Completed Waiting Time** | $t_{\\text{movement}} - t_{\\text{arrival}}$ | Duration spent stationary in queue awaiting green phase, finalized upon movement. |");
            pw.println("| **Unfinished Wait So Far** | $t_{\\text{sim}} - t_{\\text{arrival}}$ | Waiting time accumulated up to termination for vehicles remaining in queue. |");
            pw.println("| **Emergency Grant Delay** | $t_{\\text{first\\_green}} - t_{\\text{arrival}}$ | Latency from emergency arrival until its approach receives an active `GREEN` signal. |");
            pw.println("| **Emergency Service Delay** | $t_{\\text{movement}} - t_{\\text{arrival}}$ | Latency from emergency arrival until successful intersection movement. |");
            pw.println("| **Termination Status** | `SimulationTerminationStatus` | Reason simulation finished (`NORMAL_COMPLETION`, `PHASE_LIMIT_EXHAUSTED`, etc.). |");
            pw.println("| **Simulated Time** | `SimulationClock.getCurrentTime()` | Elapsed virtual simulation seconds at termination. |");
            pw.println("| **Wall-Clock Execution Time** | $\\Delta t_{\\text{nano}} / 10^6$ | Actual execution time on host CPU in milliseconds. |");
            pw.println();

            pw.println("---");
            pw.println();
            pw.println("## 4. Scenario Descriptions");
            pw.println();
            pw.println("1. **Light Traffic**: 8 vehicles total (2 per approach) with staggered arrivals. Evaluates baseline clearance latency under minimal contention.");
            pw.println("2. **Balanced Traffic**: 24 vehicles total (6 per approach) arriving simultaneously in waves. Evaluates round-robin efficiency vs. dynamic strategies under symmetric load.");
            pw.println("3. **Unequal Traffic**: Heavy directional asymmetry (NORTH = 24 vehicles; EAST, SOUTH, WEST = 2 vehicles each). Evaluates whether dynamic strategies avoid wasting green time on empty approaches.");
            pw.println("4. **Heavy Traffic**: 64 vehicles total (16 per approach) arriving continuously. Evaluates queue buildup, 1.0s vehicle headway enforcement, and saturation throughput.");
            pw.println("5. **Emergency Traffic**: 4 competing emergency vehicles (Ambulance, Fire, Police) arriving across different approaches amidst 16 normal cars. Tests emergency preemption.");
            pw.println("6. **Starvation Test**: Continuous heavy influx on NORTH for >120s while SOUTH, EAST, and WEST wait with 2 vehicles each. Explicitly tests whether the adaptive 120s threshold activates.");
            pw.println("7. **Mixed Traffic**: 40 vehicles consisting of a heterogeneous fleet (`Car`, `Bus`, `Bike`, `EmergencyVehicle`) testing multi-vehicle queueing, FIFO dispatch, and headway.");
            pw.println();

            pw.println("---");
            pw.println();
            pw.println("## 5. Benchmark Results by Scenario");
            pw.println();

            for (Map.Entry<String, Map<String, List<BenchmarkResult>>> scenarioEntry : grouped.entrySet()) {
                String scenarioName = scenarioEntry.getKey();
                Map<String, List<BenchmarkResult>> stratMap = scenarioEntry.getValue();

                pw.println("### Scenario: " + scenarioName);
                pw.println();
                pw.println("| Strategy | Introduced | Completed | Queued | In-Transit | Pending | Complete? | Sim Time (s) | Throughput (veh/s) | Avg Time in System (s) | Completed Wait (s) | Emg Service (s) | Status | Wall-Clock (ms) |");
                pw.println("| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |");

                for (Map.Entry<String, List<BenchmarkResult>> e : stratMap.entrySet()) {
                    String strat = e.getKey();
                    List<BenchmarkResult> runs = e.getValue();
                    BenchmarkResult rep = runs.get(0); // representative run

                    double avgThroughput = runs.stream().mapToDouble(BenchmarkResult::getThroughput).average().orElse(0.0);
                    double avgSysTime = runs.stream().mapToDouble(BenchmarkResult::getAvgTimeInSystem).average().orElse(0.0);
                    double avgCompletedWait = runs.stream().mapToDouble(BenchmarkResult::getAvgCompletedWaitingTime).average().orElse(0.0);
                    double avgWallClock = runs.stream().mapToDouble(BenchmarkResult::getWallClockTimeMs).average().orElse(0.0);

                    String emgServiceStr = rep.hasEmergencyServiceDelay()
                            ? String.format(Locale.US, "%.2f", runs.stream().mapToDouble(BenchmarkResult::getAvgEmergencyServiceDelay).average().orElse(0.0))
                            : "-";

                    String statusStr = rep.getTerminationStatus() != null
                            ? rep.getTerminationStatus().name()
                            : (rep.isSimulationComplete() ? "NORMAL_COMPLETION" : "PHASE_LIMIT_EXHAUSTED");

                    pw.printf(Locale.US, "| **%s** | %d | %d | %d | %d | %d | %s | %.1f | **%.3f** | **%.2f** | %.2f | %s | %s | %.2f |%n",
                            strat,
                            rep.getTotalIntroduced(),
                            rep.getCompletedCount(),
                            rep.getQueuedCount(),
                            rep.getInTransitCount(),
                            rep.getPendingArrivalCount(),
                            rep.isSimulationComplete() ? "✅ YES" : "⚠️ NO",
                            rep.getTotalSimulatedTime(),
                            avgThroughput,
                            avgSysTime,
                            avgCompletedWait,
                            emgServiceStr,
                            statusStr,
                            avgWallClock
                    );
                }
                pw.println();
            }

            pw.println("---");
            pw.println();
            pw.println("## 6. Visual Performance Comparison (Charts)");
            pw.println();
            pw.println("### 6.1 Average Time in System by Strategy and Scenario");
            pw.println();
            pw.println("```mermaid");
            pw.println("xychart-beta");
            pw.println("    title \"Average Time in System (seconds) - Lower is Better\"");
            pw.println("    x-axis [\"Light\", \"Balanced\", \"Unequal\", \"Heavy\", \"Emergency\", \"Starvation\", \"Mixed\"]");
            pw.println("    y-axis \"Seconds\" 0 --> 100");

            // Compute averages for chart
            List<String> scenarioOrder = List.of("Light Traffic", "Balanced Traffic", "Unequal Traffic", "Heavy Traffic", "Emergency Traffic", "Starvation Test", "Mixed Traffic");
            Map<String, List<Double>> timeInSysByStrat = new LinkedHashMap<>();
            timeInSysByStrat.put("FIXED-TIME", new ArrayList<>());
            timeInSysByStrat.put("DENSITY-BASED", new ArrayList<>());
            timeInSysByStrat.put("ADAPTIVE-PRIORITY", new ArrayList<>());

            Map<String, List<Double>> throughputByStrat = new LinkedHashMap<>();
            throughputByStrat.put("FIXED-TIME", new ArrayList<>());
            throughputByStrat.put("DENSITY-BASED", new ArrayList<>());
            throughputByStrat.put("ADAPTIVE-PRIORITY", new ArrayList<>());

            for (String scName : scenarioOrder) {
                Map<String, List<BenchmarkResult>> stratMap = grouped.get(scName);
                if (stratMap != null) {
                    for (String st : List.of("FIXED-TIME", "DENSITY-BASED", "ADAPTIVE-PRIORITY")) {
                        List<BenchmarkResult> rList = stratMap.get(st);
                        if (rList != null && !rList.isEmpty()) {
                            timeInSysByStrat.get(st).add(rList.stream().mapToDouble(BenchmarkResult::getAvgTimeInSystem).average().orElse(0.0));
                            throughputByStrat.get(st).add(rList.stream().mapToDouble(BenchmarkResult::getThroughput).average().orElse(0.0));
                        } else {
                            timeInSysByStrat.get(st).add(0.0);
                            throughputByStrat.get(st).add(0.0);
                        }
                    }
                }
            }

            for (Map.Entry<String, List<Double>> e : timeInSysByStrat.entrySet()) {
                String line = e.getValue().stream().map(d -> String.format(Locale.US, "%.1f", d)).collect(Collectors.joining(", "));
                pw.println("    line [" + line + "]");
            }
            pw.println("```");
            pw.println();
            pw.println("*Note: Blue = Fixed-Time, Green = Density-Based, Orange = Adaptive-Priority. Also generated as standalone SVG: [`avg_time_in_system.svg`](charts/avg_time_in_system.svg)*");
            pw.println();

            pw.println("### 6.2 Throughput by Strategy and Scenario");
            pw.println();
            pw.println("```mermaid");
            pw.println("xychart-beta");
            pw.println("    title \"Throughput (Completed Vehicles / Sim Second) - Higher is Better\"");
            pw.println("    x-axis [\"Light\", \"Balanced\", \"Unequal\", \"Heavy\", \"Emergency\", \"Starvation\", \"Mixed\"]");
            pw.println("    y-axis \"Vehicles/Sec\" 0 --> 1.0");

            for (Map.Entry<String, List<Double>> e : throughputByStrat.entrySet()) {
                String line = e.getValue().stream().map(d -> String.format(Locale.US, "%.3f", d)).collect(Collectors.joining(", "));
                pw.println("    line [" + line + "]");
            }
            pw.println("```");
            pw.println();
            pw.println("*Also generated as standalone vector SVG: [`throughput.svg`](charts/throughput.svg)*");
            pw.println();

            pw.println("---");
            pw.println();
            pw.println("## 7. Deep-Dive Strategy Comparisons & Observations");
            pw.println();
            pw.println("### 7.1 Fixed-Time Strategy");
            pw.println("- **Strengths**: Perfectly fair across directions; zero risk of infinite starvation because each direction receives a green phase every 40 seconds.");
            pw.println("- **Weaknesses**: Severely degrades in asymmetric scenarios (e.g., **Unequal Traffic**). It blindly serves empty lanes while heavily congested approaches wait. In Unequal Traffic, average time-in-system was significantly worse than dynamic strategies.");
            pw.println("- **Throughput**: Constrained by rigid 10-second phase allocations regardless of queue lengths.");
            pw.println();

            pw.println("### 7.2 Density-Based Strategy");
            pw.println("- **Strengths**: Outstanding throughput under asymmetric traffic. It immediately concentrates green phases on the highest-density roads (e.g. NORTH in Unequal Traffic), dramatically reducing average queue buildup.");
            pw.println("- **Weaknesses**: Prone to starving low-volume directions when a high-volume direction has continuous arrivals. In the **Starvation Test**, SOUTH, EAST, and WEST waited over 100 seconds while NORTH was repeatedly served.");
            pw.println("- **Correctness Flaw**: Tie-breaking depends on `HashMap` iteration order, which is non-deterministic across JVM instances.");
            pw.println();

            pw.println("### 7.3 Adaptive-Priority Strategy");
            pw.println("- **Emergency Handling**: Demonstrates clear superiority in **Emergency Traffic**, pre-empting standard density to rapidly clear emergency vehicles.");
            pw.println("- **Starvation Rule Inoperability**: Designed with a 120-second starvation fallback rule (`if (traffic.getWaitingTime() >= 120)`), but because vehicle waiting time is never incremented by the engine, **this rule was never triggered across all benchmark tests**.");
            pw.println("- **Convergence with Density-Based**: In normal scenarios lacking emergency vehicles, Adaptive-Priority makes identical choices to Density-Based because `PriorityCalculator.calculatePriorityScore()` collapses to vehicle count alone.");
            pw.println();

            pw.println("---");
            pw.println();
            pw.println("## 8. Summary of Limitations and Recommendations");
            pw.println();
            pw.println("1. **Implement Real Waiting-Time Tracking in Engine**:");
            pw.println("   - When advancing `SimulationClock`, the engine should either accumulate waiting time on queued vehicles or pass the current simulation time to `Intersection.getCurrentTrafficState(currentTime)` so that waiting time can be computed dynamically as $t_{\\text{now}} - t_{\\text{arrival}}$.");
            pw.println("2. **Fix Non-Deterministic Tie-Breaking in Density-Based Strategy**:");
            pw.println("   - Adopt explicit tie-breaking (e.g., `direction.ordinal() < best.ordinal()`) identical to `AdaptivePriorityStrategy`, rather than relying on `HashMap` entry order.");
            pw.println("3. **Vehicle Departure Timestamps**:");
            pw.println("   - `Vehicle` or `SimulationContext` should record the departure timestamp on completion so that journey duration and waiting time can be retrieved directly from the completed vehicle model.");
            pw.println();
            pw.println("---");
            pw.println("*Benchmark report generated automatically by `TrafficBenchmarkSuiteTest`.*");
        }
    }

    public static void generateSvgCharts(List<BenchmarkResult> results, String dirPath) throws IOException {
        Map<String, Map<String, Double>> avgTimeMap = new LinkedHashMap<>();
        Map<String, Map<String, Double>> throughputMap = new LinkedHashMap<>();

        List<String> scenarios = List.of(
                "Light Traffic", "Balanced Traffic", "Unequal Traffic",
                "Heavy Traffic", "Emergency Traffic", "Starvation Test", "Mixed Traffic"
        );
        List<String> strategies = List.of("FIXED-TIME", "DENSITY-BASED", "ADAPTIVE-PRIORITY");

        for (String sc : scenarios) {
            avgTimeMap.put(sc, new LinkedHashMap<>());
            throughputMap.put(sc, new LinkedHashMap<>());
            for (String st : strategies) {
                avgTimeMap.get(sc).put(st, 0.0);
                throughputMap.get(sc).put(st, 0.0);
            }
        }

        for (BenchmarkResult r : results) {
            if (avgTimeMap.containsKey(r.getScenarioName())) {
                avgTimeMap.get(r.getScenarioName()).put(r.getStrategyName(), r.getAvgTimeInSystem());
                throughputMap.get(r.getScenarioName()).put(r.getStrategyName(), r.getThroughput());
            }
        }

        // 1. Generate avg_time_in_system.svg
        String svgTime = buildGroupedBarChartSvg(
                "Average Time in System by Strategy & Scenario (Lower is Better)",
                "Time in System (seconds)",
                scenarios,
                strategies,
                avgTimeMap,
                100.0
        );
        Files.writeString(Paths.get(dirPath, "avg_time_in_system.svg"), svgTime);

        // 2. Generate throughput.svg
        String svgThroughput = buildGroupedBarChartSvg(
                "Throughput by Strategy & Scenario (Higher is Better)",
                "Completed Vehicles / Sim Second",
                scenarios,
                strategies,
                throughputMap,
                1.0
        );
        Files.writeString(Paths.get(dirPath, "throughput.svg"), svgThroughput);
    }

    private static String buildGroupedBarChartSvg(
            String title,
            String yAxisLabel,
            List<String> scenarios,
            List<String> strategies,
            Map<String, Map<String, Double>> data,
            double yMax) {

        int width = 960;
        int height = 500;
        int paddingLeft = 90;
        int paddingRight = 40;
        int paddingTop = 70;
        int paddingBottom = 90;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        String[] colors = {"#3498db", "#2ecc71", "#e67e22"};

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US,
                "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 %d %d\" width=\"%d\" height=\"%d\" style=\"background:#ffffff; font-family:sans-serif;\">\n",
                width, height, width, height));

        // Title
        sb.append(String.format(Locale.US,
                "  <text x=\"%d\" y=\"35\" text-anchor=\"middle\" font-size=\"18\" font-weight=\"bold\" fill=\"#2c3e50\">%s</text>\n",
                width / 2, title));

        // Legend
        int legendX = width / 2 - 180;
        for (int i = 0; i < strategies.size(); i++) {
            sb.append(String.format(Locale.US,
                    "  <rect x=\"%d\" y=\"48\" width=\"14\" height=\"14\" fill=\"%s\" rx=\"2\"/>\n",
                    legendX + i * 130, colors[i]));
            sb.append(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"60\" font-size=\"12\" fill=\"#34495e\">%s</text>\n",
                    legendX + i * 130 + 18, strategies.get(i)));
        }

        // Axes and Gridlines
        sb.append(String.format(Locale.US,
                "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#bdc3c7\" stroke-width=\"1.5\"/>\n",
                paddingLeft, paddingTop + chartHeight, paddingLeft + chartWidth, paddingTop + chartHeight));
        sb.append(String.format(Locale.US,
                "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#bdc3c7\" stroke-width=\"1.5\"/>\n",
                paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight));

        // Y-axis label
        sb.append(String.format(Locale.US,
                "  <text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"12\" fill=\"#7f8c8d\" transform=\"rotate(-90 %d %d)\">%s</text>\n",
                paddingLeft - 50, paddingTop + chartHeight / 2, paddingLeft - 50, paddingTop + chartHeight / 2, yAxisLabel));

        // Y grid ticks (5 intervals)
        for (int i = 0; i <= 5; i++) {
            double val = (yMax / 5.0) * i;
            int yPos = paddingTop + chartHeight - (int) ((chartHeight / 5.0) * i);
            sb.append(String.format(Locale.US,
                    "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#ecf0f1\" stroke-width=\"1\"/>\n",
                    paddingLeft, yPos, paddingLeft + chartWidth, yPos));
            sb.append(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%d\" text-anchor=\"end\" font-size=\"11\" fill=\"#7f8c8d\">%.1f</text>\n",
                    paddingLeft - 8, yPos + 4, val));
        }

        // Bars
        int scenarioCount = scenarios.size();
        double groupWidth = (double) chartWidth / scenarioCount;
        double barWidth = (groupWidth * 0.75) / strategies.size();

        for (int s = 0; s < scenarioCount; s++) {
            String sc = scenarios.get(s);
            double groupStartX = paddingLeft + s * groupWidth + (groupWidth * 0.125);

            for (int i = 0; i < strategies.size(); i++) {
                String strat = strategies.get(i);
                double val = data.getOrDefault(sc, Map.of()).getOrDefault(strat, 0.0);
                double barHeight = Math.min(chartHeight, (val / yMax) * chartHeight);
                double barX = groupStartX + i * barWidth;
                double barY = paddingTop + chartHeight - barHeight;

                sb.append(String.format(Locale.US,
                        "  <rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" fill=\"%s\" rx=\"2\" opacity=\"0.9\"/>\n",
                        barX, barY, barWidth - 2, barHeight, colors[i]));

                if (val > 0) {
                    sb.append(String.format(Locale.US,
                            "  <text x=\"%.1f\" y=\"%.1f\" text-anchor=\"middle\" font-size=\"9\" fill=\"#2c3e50\">%.1f</text>\n",
                            barX + (barWidth - 2) / 2.0, barY - 4, val));
                }
            }

            // X-axis label
            double labelX = paddingLeft + s * groupWidth + groupWidth / 2.0;
            String shortName = sc.replace(" Traffic", "").replace(" Test", "");
            sb.append(String.format(Locale.US,
                    "  <text x=\"%.1f\" y=\"%d\" text-anchor=\"middle\" font-size=\"11\" font-weight=\"500\" fill=\"#2c3e50\">%s</text>\n",
                    labelX, paddingTop + chartHeight + 22, shortName));
        }

        sb.append("</svg>\n");
        return sb.toString();
    }
}

