package org.javalabs.tools.cbs2sql.cli.storage.impl;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;
import reactor.core.publisher.Flux;

/**
 * A query strategy for batch manipulation.
 *
 * @author schan280
 */
public class BatchQueryStrategy extends QueryStrategy {
    
    BatchQueryStrategy(CouchbaseInternal storage) {
        super(storage);
    }
    
    @Override
    public Result select(CBQueryConfig query) throws CouchbaseException {
        // Query will be in the form of: select count(*) from `{bucket}`.{scope}.{collection} where {condition1} {condition2} ...
        // 1. Execute the query
        QueryOptions options = QueryOptions.queryOptions();
        if (query.getParameters() != null) {
            JsonObject params = JsonObject.create();
            for (Map.Entry<String, Object> me : query.getParameters().entrySet()) {
                params.put(me.getKey(), me.getValue());
            }
            options.parameters(params);
        }
        if (query.isVerbose()) {
            ConsoleWriter.println(String.format("Raw query: %s. Param(s): %s", query.getN1ql(), query.getParameters()));
        }
        QueryResult queryResult = storage().cluster().query(query.getN1ql(), options);
        
        // 2. Process the results
        if (query.getRaw()) {
            Result<byte[]> result = new Result(queryResult.metaData());
            for (byte[] buff : queryResult.rowsAs(byte[].class)) {
                result.record(buff);
            }
            return result;
        }
        Result<JsonObject> result = new Result(queryResult.metaData());
        for (JsonObject row : queryResult.rowsAsObject()) {
            result.record(row);
        }
        return result;
    }
    
    @Override
    public Result insert(CBQueryConfig query) throws CouchbaseException {
        Collection collection = collection(query);
        List<MutationResult> results = Flux.fromIterable(query.getRecords())
                .flatMap(document -> collection.reactive().insert(document.getString("id"), document.get("content")))
                .collectList()
                .block(); // Wait until all operations have completed.

        Result result = new Result();
        for (MutationResult mutResult : results) {
            result.cas(mutResult.cas());
        }
        return result;
    }
    
    @Override
    public Result update(CBQueryConfig query) throws CouchbaseException {
        Collection collection = collection(query);
        List<MutationResult> results = Flux.fromIterable(query.getRecords())
                .flatMap(document -> collection.reactive().upsert(document.getString("id"), document.get("content")))
                .collectList()
                .block(); // Wait until all operations have completed.

        Result result = new Result();
        for (MutationResult mutResult : results) {
            result.cas(mutResult.cas());
        }
        return result;
    }
    
    @Override
    public Result delete(CBQueryConfig query) throws CouchbaseException {
        // Query must be in the form of - DELETE FROM `{bucket}`.`{scope}`.`{collection}` WHERE key = $key"
        QueryOptions options = QueryOptions.queryOptions();
        if (query.getParameters() != null) {
            JsonObject params = JsonObject.create();
            for (Map.Entry<String, Object> me : query.getParameters().entrySet()) {
                params.put(me.getKey(), me.getValue());
            }
            options.parameters(params);
        }
        
        QueryResult queryResult = storage().cluster().query(query.getN1ql(), options);
        return new Result(queryResult.metaData());
    }
}
