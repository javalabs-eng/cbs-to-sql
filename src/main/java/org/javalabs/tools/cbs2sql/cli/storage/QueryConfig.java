package org.javalabs.tools.cbs2sql.cli.storage;

import java.util.Map;

/**
 * Defines the configuration required to execute a database query.
 *
 * <p>A {@code QueryConfig} encapsulates the query resource, execution
 * parameters, verbosity settings, and any records associated with the
 * operation. Implementations are storage-specific and provide the
 * information required by a {@code Storage} implementation to execute
 * a query.</p>
 *
 * @author schan280
 */
public interface QueryConfig {

    /**
     * Returns the unique identifier for this query configuration.
     *
     * @return the query key
     */
    String getKey();

    /**
     * Returns the target resource against which the query is executed.
     *
     * <p>The meaning of the resource depends on the underlying storage
     * implementation and may represent a table, collection, bucket,
     * document, or other logical data source.</p>
     *
     * @return the query resource
     */
    String getResource();

    /**
     * Indicates whether verbose logging should be enabled during query
     * execution.
     *
     * @return {@code true} to enable verbose output; otherwise {@code false}
     */
    Boolean isVerbose();

    /**
     * Returns the named parameters to be bound to the query before execution.
     *
     * @return a map of parameter names to values, or {@code null} if the query
     *         does not require any parameters
     */
    Map<String, Object> getParameters();

    /**
     * Returns the records associated with this query configuration.
     *
     * <p>The returned object is implementation-specific and may represent a
     * single record, a collection of records, or another data structure used
     * by the underlying storage implementation.</p>
     *
     * @return the records to be processed, or {@code null} if not applicable
     */
    Object getRecords();
}