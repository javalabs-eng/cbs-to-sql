package org.javalabs.tools.cbs2sql.cli.storage.impl;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;

/**
 *
 * @author schan280
 */
public class KeyedQueryStrategy extends QueryStrategy {
    
    KeyedQueryStrategy(CouchbaseInternal storage) {
        super(storage);
    }
    
    @Override
    public Result select(CBQueryConfig query) throws CouchbaseException {
        GetResult getResult = collection(query).get(query.getKey());
        return new Result()
                .cas(getResult.cas())
                .record(getResult.contentAsObject());
    }
    
    @Override
    public Result insert(CBQueryConfig query) throws CouchbaseException {
        Collection collection = collection(query);
        Result result = new Result();
        
        for (JsonObject record : query.getRecords()) {
            MutationResult mutResult = collection.insert(record.getString("id"), record.getObject("content"));
            result.cas(mutResult.cas());
        }
        return result;
    }
    
    @Override
    public Result update(CBQueryConfig query) throws CouchbaseException {
        Collection collection = collection(query);
        Result result = new Result();
        
        for (JsonObject record : query.getRecords()) {
            MutationResult mutResult = collection.upsert(record.getString("id"), record.getObject("content"));
            result.cas(mutResult.cas());
        }
        return result;
    }
    
    @Override
    public Result delete(CBQueryConfig query) throws CouchbaseException {
        MutationResult mutResult = collection(query).remove(query.getKey());
        return new Result()
                .cas(mutResult.cas());
    }
}
