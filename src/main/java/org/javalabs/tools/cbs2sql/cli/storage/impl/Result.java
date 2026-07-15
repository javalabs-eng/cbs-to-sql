package org.javalabs.tools.cbs2sql.cli.storage.impl;

import com.couchbase.client.java.query.QueryMetaData;
import com.couchbase.client.java.query.QueryStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a Couchbase query operation.
 *
 * <p>A {@code Result} encapsulates the records returned by a query together
 * with the associated Couchbase query metadata, CAS (Compare-And-Swap) values,
 * and optional result size information. The class provides a fluent API for
 * adding records and CAS values as they are processed.</p>
 *
 * <p>This class is primarily intended for use by the Couchbase storage
 * implementation when returning query results to higher layers of the
 * application.</p>
 *
 * @param <T> the type of records contained in the result
 *
 * @author schan280
 */
public class Result<T> {
    
    private final QueryMetaData metadata;
    private final List<T> records = new ArrayList<>();
    private final List<Long> casList = new ArrayList<>();
    
    private Long maxSize = 0L;
    
    public Result() {
        this(null);
    }
    
    public Result(QueryMetaData metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the metadata associated with the executed query.
     *
     * @return the query metadata, or {@code null} if no metadata is available
     */
    public QueryMetaData metadata() {
        return metadata;
    }

    /**
     * Returns the records produced by the query.
     *
     * @return the list of result records
     */
    public List<T> records() {
        return records;
    }

    /**
     * Adds a record to this result.
     *
     * <p>This method supports fluent method chaining.</p>
     *
     * @param record the record to add
     * @return this result instance
     */
    public Result record(T record) {
        this.records.add(record);
        return this;
    }
    
    /**
     * Returns the CAS (Compare-And-Swap) values associated with the returned
     * documents.
     *
     * @return the list of CAS values
     */
    public List<Long> cas() {
        return casList;
    }

    /**
     * Adds a CAS (Compare-And-Swap) value to this result.
     *
     * <p>This method supports fluent method chaining.</p>
     *
     * @param cas the CAS value to add
     * @return this result instance
     */
    public Result cas(Long cas) {
        this.casList.add(cas);
        return this;
    }
    
    /**
     * Returns the execution status of the query.
     *
     * <p>The status is obtained from the associated query metadata.</p>
     *
     * @return the query execution status, or {@code null} if metadata is not
     *         available
     */
    public QueryStatus status() {
        if (metadata != null) {
            return metadata.status();
        }
        return null;
    }

    /**
     * Returns the maximum number of records associated with this result.
     *
     * <p>This value may represent the total number of records available,
     * independent of the number currently contained in {@link #records()}.</p>
     *
     * @return the maximum result size
     */
    public Long getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum number of records associated with this result.
     *
     * @param maxSize the maximum result size
     */
    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }
}
