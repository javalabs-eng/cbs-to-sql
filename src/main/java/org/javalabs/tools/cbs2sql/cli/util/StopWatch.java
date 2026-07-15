package org.javalabs.tools.cbs2sql.cli.util;

/**
 * Utility Timer Class.
 */
public final class StopWatch {

    private long startNanoTs = 0L;
    private long startMilliTs = 0L;

    private long endNanoTs = 0L;
    private long endMilliTs = 0L;

    private StopWatch() {}

    /**
     * Create and return a new timer instance.
     * @return StopWatch
     */
    public static StopWatch newTimer() {
        return new StopWatch();
    }

    /**
     * Start the timer.
     */
    public void start() {
        //startNanoTs = System.nanoTime();
        startMilliTs = System.currentTimeMillis();
    }

    /**
     * Stop the timer.
     */
    public void stop() {
        //endNanoTs = System.nanoTime();
        endMilliTs = System.currentTimeMillis();
    }

    /**
     * Return the elapsed time in nano second.
     * @return long
     */
    public long elapsedTimeNanos() {
        return endNanoTs - startNanoTs;
    }

    /**
     * Return the elapsed time in nano second.
     * @return long
     */
    public long elapsedTimeMillis() {
        return endMilliTs - startMilliTs;
    }

    /**
     * Return the current elapsed time in milli second.
     * @return long
     */
    public long currentElapsedTimeMillis() {
        return System.currentTimeMillis() - startMilliTs;
    }

    /**
     * Reset this timer.
     */
    public void reset() {
        startNanoTs = 0L;
        startMilliTs = 0L;
        endNanoTs = 0L;
        endMilliTs = 0L;
    }
}