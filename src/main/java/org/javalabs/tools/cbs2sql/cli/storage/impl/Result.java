package org.javalabs.tools.cbs2sql.cli.storage.impl;

import com.couchbase.client.java.query.QueryMetaData;
import com.couchbase.client.java.query.QueryStatus;
import java.util.ArrayList;
import java.util.List;

/**
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

    public QueryMetaData metadata() {
        return metadata;
    }

    public List<T> records() {
        return records;
    }

    public Result record(T record) {
        this.records.add(record);
        return this;
    }

    public List<Long> cas() {
        return casList;
    }

    public Result cas(Long cas) {
        this.casList.add(cas);
        return this;
    }
    
    public QueryStatus status() {
        if (metadata != null) {
            return metadata.status();
        }
        return null;
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }
}
