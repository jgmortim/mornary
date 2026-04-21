package com.mornary.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Service for printing progress updates to the console.
 *
 * @author John Mortimore
 */
public class PrintService {

    private final Instant startTime;
    private Instant lastProgressPrint;

    /**
     * Constructs a new Print Service.
     */
    public PrintService() {
        this.startTime = Instant.now();
        this.lastProgressPrint = this.startTime;
    }

    /**
     * Prints the current progress percentage to the console.
     *
     * @param workUnitsWritten The number of completed work units that have been written to the output file.
     * @param totalWorkUnits   The total number of work units in the operation.
     */
    public void printProgress(long workUnitsWritten, long totalWorkUnits) {
        Instant now = Instant.now();

        long secondsSinceLastPrint = Duration.between(this.lastProgressPrint, now).getSeconds();
        final boolean jobCompleted = workUnitsWritten == totalWorkUnits;

        // Print at most once per second, unless this is the final work unit.
        if (secondsSinceLastPrint >= 1 || jobCompleted) {
            double progress = ((double) workUnitsWritten / totalWorkUnits);
            double percent = progress * 100.0;

            long seconds = Duration.between(this.startTime, now).getSeconds();
            long estimatedTotalSeconds = (long) (seconds / progress);
            long estimatedSecondsRemaining = estimatedTotalSeconds - seconds;

            System.out.printf("\rWork Units Completed: %d of %d (%.2f%%). Elapsed Time: %ss. Estimated Time Remaining: %ss",
                workUnitsWritten, totalWorkUnits, percent, seconds, estimatedSecondsRemaining);
            this.lastProgressPrint = now;
            if (jobCompleted) {
                System.out.println(System.lineSeparator() + "Job Completed at: " + LocalDateTime.now());
            }
        }
    }
}
