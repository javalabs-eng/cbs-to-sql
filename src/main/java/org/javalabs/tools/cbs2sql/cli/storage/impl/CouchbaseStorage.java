package org.javalabs.tools.cbs2sql.cli.storage.impl;

import org.javalabs.tools.cbs2sql.cli.storage.QueryConfig;
import org.javalabs.tools.cbs2sql.cli.storage.Status;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;

/**
 * A concrete implementation of couchbase storage.
 *
 * @author schan280
 */
public class CouchbaseStorage extends CouchbaseInternal {
    
    private CouchbaseStorage() {}
    
    public static final CouchbaseStorage get() {
        return new CouchbaseStorage();
    }

    @Override
    public Status ddlOps(QueryConfig query) {
        try {
            CBQueryConfig cbQuery = (CBQueryConfig) query;
            QueryStrategy strategy = strategy(Boolean.FALSE);
            
            Result result = strategy.ddlCreate(cbQuery);
            return status(result.status());
        }
        catch (CouchbaseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<byte[]> export(QueryConfig query) {
        try {
            CBQueryConfig cbQuery = (CBQueryConfig) query;
            QueryStrategy strategy = strategy(cbQuery.getBatch());
            
            Result<byte[]> result = strategy.select(cbQuery);
            
            List<byte[]> list = result.records();
            for (byte[] row : list) {
                // Address newline character
                for (int i = 0; i < row.length; i ++) {
                    if (row[i] == 10) {
                        row[i] = 32;    // Replace with empty space
                    }
                }
            }
            return list;
        }
        catch (CouchbaseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map> select(QueryConfig query) {
        try {
            CBQueryConfig cbQuery = (CBQueryConfig) query;
            QueryStrategy strategy = strategy(cbQuery.getBatch());
            
            Result<JsonObject> result = strategy.select(cbQuery);
            List<Map> list = new ArrayList<>();
            for (JsonObject row : result.records()) {
                list.add(row.toMap());
            }
            return list;
        }
        catch (CouchbaseException e) {
            throw e;
        }
    }

    @Override
    public Status insert(QueryConfig query) {
        try {
            CBQueryConfig cbQuery = (CBQueryConfig) query;
            QueryStrategy strategy = strategy(cbQuery.getBatch());
            
            Result result = strategy.insert(cbQuery);
            if (query.isVerbose()) {
                ConsoleWriter.println(String.format("Cas(s) from insert: %s", result.cas()));
            }
            return status(result.status());
            
        }
        catch (CouchbaseException e) {
            throw e;
        }
    }

    @Override
    public Status update(QueryConfig query) {
        try {
            CBQueryConfig cbQuery = (CBQueryConfig) query;
            QueryStrategy strategy = strategy(cbQuery.getBatch());
            
            Result result = strategy.update(cbQuery);
            if (query.isVerbose()) {
                ConsoleWriter.println(String.format("Cas(s) from update: %s", result.cas()));
            }
            return status(result.status());
        }
        catch (CouchbaseException e) {
            throw e;
        }
    }
    
    @Override
    public Status delete(QueryConfig query) {
        try {
            CBQueryConfig cbQuery = (CBQueryConfig) query;
            QueryStrategy strategy = strategy(cbQuery.getBatch());
            
            Result result = strategy.delete(cbQuery);
            if (query.isVerbose()) {
                ConsoleWriter.println(String.format("Cas(s) from delete: %s", result.cas()));
            }
            return status(result.status());
        }
        catch (CouchbaseException e) {
            throw e;
        }
    }
    
    private Status status(QueryStatus status) {
        if (status == null) {
            return Status.SUCCESS;
        }
        return Enum.valueOf(Status.class, status.name());
    }
}
