package org.javalabs.tools.cbs2sql.cli.storage;

/**
 * Represents the possible execution states of an operation.
 *
 * <p>The status indicates the current lifecycle stage of a task, such as
 * import, export, or synchronization, and can be used to monitor progress,
 * report outcomes, or determine the next processing step.</p>
 *
 * <p>The meaning of each status is documented alongside its corresponding
 * enum constant.</p>
 *
 * @author schan280
 */
public enum Status {
    
    ACCEPTED,
    RUNNING,
    SUCCESS,
    ERRORS,
    COMPLETED,
    STOPPED,
    TIMEOUT,
    CLOSED,
    FATAL,
    ABORTED,
    UNKNOWN;
}