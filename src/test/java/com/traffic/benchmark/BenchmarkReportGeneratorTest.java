package com.traffic.benchmark;

import com.traffic.control.Direction;
import com.traffic.simulation.engine.SimulationTerminationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BenchmarkReportGeneratorTest {

    private static final List<String> EXPECTED_ORIGINAL_21_COLUMNS = List.of(
            "Scenario",
            "Strategy",
            "RunIndex",
            "Introduced",
            "Completed",
            "Remaining",
            "Pending",
            "IsComplete",
            "AccountingValid",
            "SimulatedTimeSec",
            "WallClockTimeMs",
            "ThroughputVehPerSec",
            "AvgTimeInSystemSec",
            "MaxTimeInSystemSec",
            "MinTimeInSystemSec",
            "AvgEmergencyTimeInSystemSec",
            "MaxEmergencyTimeInSystemSec",
            "EmergencyResponseDelaySec",
            "ReportedInternalAvgWaitSec",
            "ReportedInternalMaxWaitSec",
            "Starvation120sTriggered"
    );

    private static final List<String> EXPECTED_APPENDED_13_COLUMNS = List.of(
            "queuedCount",
            "inTransitCount",
            "pendingArrivalCount",
            "remainingCount",
            "completionRate",
            "accountingValid",
            "avgCompletedWaitingTime",
            "maxCompletedWaitingTime",
            "avgUnfinishedWaitSoFar",
            "maxUnfinishedWaitSoFar",
            "avgEmergencyGrantDelay",
            "avgEmergencyServiceDelay",
            "terminationStatus"
    );

    @Test
    @DisplayName("CSV header preserves original 21 columns and appends all 13 new columns in exact order")
    void testCsvHeaderAndColumnOrdering(@TempDir Path tempDir) throws IOException {
        Path csvPath = tempDir.resolve("test-benchmark.csv");

        BenchmarkResult sample = createSampleResult(
                "Light Traffic", "FIXED-TIME", 1,
                8, 8, 0, 0, 0, 0, 1.0,
                40.0, 1.5, 0.2,
                6.0, 14.0, 0.0,
                3.5, 7.0, 0.0, 0.0,
                0.0, 0.0, 0.0,
                0.0, 0.0,
                SimulationTerminationStatus.NORMAL_COMPLETION,
                Collections.emptyList()
        );

        BenchmarkReportGenerator.generateCsv(List.of(sample), csvPath.toString());

        List<String> lines = Files.readAllLines(csvPath);
        assertFalse(lines.isEmpty(), "CSV must not be empty");

        String headerLine = lines.get(0);
        String[] headers = headerLine.split(",", -1);

        assertEquals(34, headers.length, "CSV must have exactly 34 columns (21 original + 13 appended)");

        // 1. Verify original 21 columns in exact order
        for (int i = 0; i < 21; i++) {
            assertEquals(EXPECTED_ORIGINAL_21_COLUMNS.get(i), headers[i],
                    "Original column " + i + " must match backward-compatible header");
        }

        // 2. Verify appended 13 columns in exact order
        for (int i = 0; i < 13; i++) {
            assertEquals(EXPECTED_APPENDED_13_COLUMNS.get(i), headers[21 + i],
                    "Appended column " + i + " (index " + (21 + i) + ") must match expected name");
        }
    }

    @Test
    @DisplayName("Correct values exported for newly appended metrics")
    void testCorrectValuesForNewlyExportedMetrics(@TempDir Path tempDir) throws IOException {
        Path csvPath = tempDir.resolve("test-metrics.csv");

        VehicleMetricRecord emgRecord = new VehicleMetricRecord(
                "EMG-1", "EMERGENCY", Direction.NORTH, 1.0, 5.0, 4.0, 0.0,
                true, true, 2.40, 3.60, null
        );

        BenchmarkResult result = new BenchmarkResult(
                "Truncated Scenario",
                "ADAPTIVE-PRIORITY",
                2,
                20, // totalIntroduced
                15, // completedCount
                3,  // queuedCount
                1,  // inTransitCount
                4,  // remainingCount
                1,  // pendingArrivalCount
                0.75, // completionRate
                70.0, // totalSimulatedTime
                4.25, // wallClockTimeMs
                0.2143, // throughput
                18.5, 35.0, 2.0,
                4.50, // avgCompletedWaitingTime
                8.25, // maxCompletedWaitingTime
                3.10, // avgUnfinishedWaitSoFar
                5.20, // maxUnfinishedWaitSoFar
                4.0, 4.0,
                2.40, // avgEmergencyGrantDelay
                3.60, // avgEmergencyServiceDelay
                3.60, // emergencyResponseTimeToService
                0.0, 0.0, false,
                SimulationTerminationStatus.PHASE_LIMIT_EXHAUSTED,
                List.of(emgRecord)
        );

        BenchmarkReportGenerator.generateCsv(List.of(result), csvPath.toString());

        List<String> lines = Files.readAllLines(csvPath);
        assertEquals(2, lines.size());

        String[] row = lines.get(1).split(",", -1);
        assertEquals(34, row.length);

        // Verify newly exported columns (indices 21 through 33)
        assertEquals("3", row[21], "queuedCount");
        assertEquals("1", row[22], "inTransitCount");
        assertEquals("1", row[23], "pendingArrivalCount");
        assertEquals("4", row[24], "remainingCount");
        assertEquals("0.7500", row[25], "completionRate");
        assertEquals("true", row[26], "accountingValid");
        assertEquals("4.50", row[27], "avgCompletedWaitingTime");
        assertEquals("8.25", row[28], "maxCompletedWaitingTime");
        assertEquals("3.10", row[29], "avgUnfinishedWaitSoFar");
        assertEquals("5.20", row[30], "maxUnfinishedWaitSoFar");
        assertEquals("2.40", row[31], "avgEmergencyGrantDelay");
        assertEquals("3.60", row[32], "avgEmergencyServiceDelay");
        assertEquals("PHASE_LIMIT_EXHAUSTED", row[33], "terminationStatus");
    }

    @Test
    @DisplayName("Unavailable optional emergency delays are exported as empty string without converting to 0.00")
    void testUnavailableOptionalValuesExportedAsEmpty(@TempDir Path tempDir) throws IOException {
        Path csvPath = tempDir.resolve("test-unavailable.csv");

        // Normal vehicle only, no emergency vehicles
        VehicleMetricRecord carRecord = new VehicleMetricRecord(
                "CAR-1", "CAR", Direction.NORTH, 0.0, 4.0, 4.0, 0.0,
                true, false, null, null, null
        );

        BenchmarkResult resultWithoutEmergency = new BenchmarkResult(
                "Normal Traffic", "FIXED-TIME", 1,
                1, 1, 0, 0, 0, 0, 1.0,
                10.0, 1.0, 0.1,
                4.0, 4.0, 4.0,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0,
                0.0, // avgEmergencyGrantDelay (unavailable, no observations)
                0.0, // avgEmergencyServiceDelay (unavailable, no observations)
                0.0,
                0.0, 0.0, false,
                SimulationTerminationStatus.NORMAL_COMPLETION,
                List.of(carRecord)
        );

        assertFalse(resultWithoutEmergency.hasEmergencyGrantDelay());
        assertFalse(resultWithoutEmergency.hasEmergencyServiceDelay());
        assertTrue(resultWithoutEmergency.getOptionalEmergencyGrantDelay().isEmpty());
        assertTrue(resultWithoutEmergency.getOptionalEmergencyServiceDelay().isEmpty());

        BenchmarkReportGenerator.generateCsv(List.of(resultWithoutEmergency), csvPath.toString());

        List<String> lines = Files.readAllLines(csvPath);
        String[] row = lines.get(1).split(",", -1);

        // Columns 31 and 32 must be empty string, NOT "0.00"
        assertEquals("", row[31], "Unavailable avgEmergencyGrantDelay must be empty string");
        assertEquals("", row[32], "Unavailable avgEmergencyServiceDelay must be empty string");
    }

    @Test
    @DisplayName("Valid 0.0 emergency delays are preserved as 0.00 and not treated as unavailable")
    void testValidZeroEmergencyDelaysPreservedAsZero(@TempDir Path tempDir) throws IOException {
        Path csvPath = tempDir.resolve("test-zero-delays.csv");

        // Emergency vehicle arrived during green and moved at t=0.0 -> valid 0.0 measurements
        VehicleMetricRecord emgRecord = new VehicleMetricRecord(
                "EMG-FAST", "EMERGENCY", Direction.NORTH, 0.0, 0.0, 0.0, 0.0,
                true, true, 0.0, 0.0, null
        );

        BenchmarkResult resultWithZeroDelays = new BenchmarkResult(
                "Emergency Green Arrival", "FIXED-TIME", 1,
                1, 1, 0, 0, 0, 0, 1.0,
                10.0, 1.0, 0.1,
                0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0,
                0.0, // avgEmergencyGrantDelay = 0.0 (valid observation)
                0.0, // avgEmergencyServiceDelay = 0.0 (valid observation)
                0.0,
                0.0, 0.0, false,
                SimulationTerminationStatus.NORMAL_COMPLETION,
                List.of(emgRecord)
        );

        assertTrue(resultWithZeroDelays.hasEmergencyGrantDelay());
        assertTrue(resultWithZeroDelays.hasEmergencyServiceDelay());
        assertTrue(resultWithZeroDelays.getOptionalEmergencyGrantDelay().isPresent());
        assertEquals(0.0, resultWithZeroDelays.getOptionalEmergencyGrantDelay().getAsDouble(), 1e-6);

        BenchmarkReportGenerator.generateCsv(List.of(resultWithZeroDelays), csvPath.toString());

        List<String> lines = Files.readAllLines(csvPath);
        String[] row = lines.get(1).split(",", -1);

        assertEquals("0.00", row[31], "Valid zero grant delay must be formatted as 0.00");
        assertEquals("0.00", row[32], "Valid zero service delay must be formatted as 0.00");
    }

    @Test
    @DisplayName("Original 21 columns remain backward-compatible in values and format")
    void testBackwardCompatibilityOfOriginalColumns(@TempDir Path tempDir) throws IOException {
        Path csvPath = tempDir.resolve("test-backward-compat.csv");

        BenchmarkResult sample = new BenchmarkResult(
                "Balanced Traffic",
                "DENSITY-BASED",
                1,
                24, // totalIntroduced
                24, // completedCount
                0,  // queuedCount
                0,  // inTransitCount
                0,  // remainingCount
                0,  // pendingArrivalCount
                1.0, // completionRate
                60.0, // totalSimulatedTime
                2.10, // wallClockTimeMs
                0.4000, // throughput
                15.17, 32.0, 0.0,
                10.5, 20.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, false,
                SimulationTerminationStatus.NORMAL_COMPLETION,
                Collections.emptyList()
        );

        BenchmarkReportGenerator.generateCsv(List.of(sample), csvPath.toString());

        List<String> lines = Files.readAllLines(csvPath);
        String[] row = lines.get(1).split(",", -1);

        assertEquals("Balanced Traffic", row[0]);
        assertEquals("DENSITY-BASED", row[1]);
        assertEquals("1", row[2]);
        assertEquals("24", row[3]);
        assertEquals("24", row[4]);
        assertEquals("0", row[5]);
        assertEquals("0", row[6]);
        assertEquals("true", row[7]);
        assertEquals("true", row[8]);
        assertEquals("60.00", row[9]);
        assertEquals("2.100", row[10]);
        assertEquals("0.4000", row[11]);
        assertEquals("15.17", row[12]);
        assertEquals("32.00", row[13]);
        assertEquals("0.00", row[14]);
        assertEquals("0.00", row[15]);
        assertEquals("0.00", row[16]);
        assertEquals("0.00", row[17]);
        assertEquals("0.00", row[18]);
        assertEquals("0.00", row[19]);
        assertEquals("false", row[20]);
    }

    @Test
    @DisplayName("Markdown report generates successfully with new metric definitions and scenario tables")
    void testMarkdownReportIncludesNewMetrics(@TempDir Path tempDir) throws IOException {
        Path reportPath = tempDir.resolve("benchmark-report.md");

        BenchmarkResult sample = new BenchmarkResult(
                "Light Traffic",
                "FIXED-TIME",
                1,
                8, 8, 0, 0, 0, 0, 1.0,
                40.0, 1.0, 0.20,
                6.0, 14.0, 0.0,
                2.0, 4.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, false,
                SimulationTerminationStatus.NORMAL_COMPLETION,
                Collections.emptyList()
        );

        BenchmarkReportGenerator.generateMarkdown(List.of(sample), reportPath.toString());

        assertTrue(Files.exists(reportPath));
        String content = Files.readString(reportPath);

        // Check for new metric definitions in Section 3
        assertTrue(content.contains("Queued Vehicles"));
        assertTrue(content.contains("In-Transit Vehicles"));
        assertTrue(content.contains("Pending Arrivals"));
        assertTrue(content.contains("Completion Rate"));
        assertTrue(content.contains("Completed Waiting Time"));
        assertTrue(content.contains("Unfinished Wait So Far"));
        assertTrue(content.contains("Emergency Grant Delay"));
        assertTrue(content.contains("Emergency Service Delay"));
        assertTrue(content.contains("Termination Status"));

        // Check for updated Section 5 table headers
        assertTrue(content.contains("Completed Wait (s)"));
        assertTrue(content.contains("Status"));
    }

    private BenchmarkResult createSampleResult(
            String scenario, String strategy, int runIndex,
            int totalIntroduced, int completedCount, int queuedCount, int inTransitCount, int remainingCount, int pendingArrivals, double completionRate,
            double simTime, double wallClock, double throughput,
            double avgTimeInSys, double maxTimeInSys, double minTimeInSys,
            double avgWait, double maxWait, double avgUnfinished, double maxUnfinished,
            double avgEmgTime, double maxEmgTime, double emgGrant, double emgService, double emgResponse,
            SimulationTerminationStatus status,
            List<VehicleMetricRecord> records) {

        return new BenchmarkResult(
                scenario, strategy, runIndex,
                totalIntroduced, completedCount, queuedCount, inTransitCount, remainingCount, pendingArrivals, completionRate,
                simTime, wallClock, throughput,
                avgTimeInSys, maxTimeInSys, minTimeInSys,
                avgWait, maxWait, avgUnfinished, maxUnfinished,
                avgEmgTime, maxEmgTime, emgGrant, emgService, emgResponse,
                0.0, 0.0, false,
                status, records
        );
    }
}

