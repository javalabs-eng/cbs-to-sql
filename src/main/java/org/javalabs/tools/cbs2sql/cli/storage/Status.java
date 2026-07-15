package org.javalabs.tools.cbs2sql.cli.storage;

/**
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