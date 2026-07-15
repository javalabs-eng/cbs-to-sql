package org.javalabs.tools.cbs2sql.cli.storage;

import org.javalabs.tools.cbs2sql.cli.storage.config.CouchbaseConfig;
import java.util.List;
import java.util.Map;

/**
 * A generic storage interface.
 * 
 * <p>
 * Defines the contract for a storage provider capable of performing initialization, lifecycle management, 
 * connectivity checks, schema operations and CRUD operations.
 * 
 * <p>
 * Implementations may represent different storage backends (for example, Couchbase or other databases) 
 * while exposing a common API for data access.
 * 
 * @author schan280
 */
public interface Storage {

    /**
     * Initializes the storage implementation using the provided configuration.
     *
     * @param config the configuration string required to initialize the storage
     */
    void init(String config);

    /**
     * Initializes the storage implementation using a Couchbase-specific
     * configuration.
     *
     * @param cbConfig the Couchbase configuration
     */
    void init(CouchbaseConfig cbConfig);

    /**
     * Returns the host or endpoint associated with the storage implementation.
     *
     * @return the storage host or endpoint
     */
    String host();

    /**
     * Verifies connectivity to the underlying storage system.
     * <p>
     * Implementations should perform a health check and throw an exception if
     * the storage is unavailable.
     */
    void ping();

    /**
     * Performs graceful shutdown and releases any resources held by the
     * storage implementation.
     */
    void windUp();

    /**
     * Executes a data definition language (DDL) operation.
     *
     * @param query the configuration describing the DDL operation
     * @return the execution status of the operation
     */
    Status ddlOps(QueryConfig query);

    /**
     * Exports data matching the supplied query.
     *
     * @param query the query configuration describing the data to export
     * @return a list of exported records as byte arrays
     */
    List<byte[]> export(QueryConfig query);

    /**
     * Executes a read operation and returns the matching records.
     *
     * @param query the query configuration describing the selection criteria
     * @return a list of records represented as maps of column names to values
     */
    List<Map> select(QueryConfig query);

    /**
     * Inserts data into the storage.
     *
     * @param query the query configuration describing the insert operation
     * @return the execution status of the operation
     */
    Status insert(QueryConfig query);

    /**
     * Updates existing data in the storage.
     *
     * @param query the query configuration describing the update operation
     * @return the execution status of the operation
     */
    Status update(QueryConfig query);

    /**
     * Deletes data from the storage.
     *
     * @param query the query configuration describing the delete operation
     * @return the execution status of the operation
     */
    Status delete(QueryConfig query);
}