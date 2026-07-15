package org.javalabs.tools.cbs2sql.cli.storage.impl;

import com.couchbase.client.core.error.CollectionNotFoundException;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.manager.collection.CollectionManager;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.manager.collection.ScopeSpec;
import java.util.List;

/**
 *
 * @author schan280
 */
public abstract class QueryStrategy {
    
    private final CouchbaseInternal storage;
    
    protected QueryStrategy(CouchbaseInternal storage) {
        this.storage = storage;
    }

    public CouchbaseInternal storage() {
        return storage;
    }
    
    protected Collection collection(CBQueryConfig query) {
        String bucketName = storage().getBucket(query.getBucket());
        if (bucketName == null) {
            throw new RuntimeException("Either bucket name is not specified, or no default bucket is configured");
        }
        String scopeName = storage().getScope(bucketName, query.getScope());
        if (scopeName == null) {
            throw new RuntimeException("Either scope name is not specified, or no default scope is configured");
        }
        String collectionName = storage().getCollection(bucketName, scopeName, query.getResource());
        
        CollectionManager mgr = storage().cluster().bucket(bucketName).collections();
        List<ScopeSpec> scopes = mgr.getAllScopes();
        Boolean found = Boolean.FALSE;

        // Iterate through scopes and collections to find a match
        for (ScopeSpec scope : scopes) {
            if (scope.name().equals(scopeName)) {
                for (CollectionSpec collection : scope.collections()) {
                    if (collection.name().equals(collectionName)) {
                        found = Boolean.TRUE; // Found the collection
                    }
                }
            }
        }
        if (found) {
            return storage().cluster()
                    .bucket(bucketName)
                    .scope(scopeName)
                    .collection(collectionName);
        }
        throw new CollectionNotFoundException(collectionName);
    }
    
    /**
     * Create the schema object (scope, collection etc) in a couchbase cluster.
     * @param query
     * @return Result
     * @throws CouchbaseException 
     */
    public Result ddlCreate(CBQueryConfig query) throws CouchbaseException {
        String bucketName = storage().getBucket(query.getBucket());
        if (bucketName == null) {
            throw new RuntimeException("Either bucket name is not specified, or no default bucket is configured");
        }
        String scopeName = storage().getScope(bucketName, query.getScope());
        if (scopeName == null) {
            throw new RuntimeException("Either scope name is not specified, or no default scope is configured");
        }
        String collectionName = storage().getCollection(bucketName, scopeName, query.getResource());
        
        CollectionManager mgr = storage().cluster().bucket(bucketName).collections();
        mgr.createCollection(scopeName, collectionName);
        
        return new Result();
    }
    
    /**
     * Api to read the data from underlying couchbase storage.
     * 
     * @param query
     * @return Result
     * @throws CouchbaseException 
     */
    public abstract Result select(CBQueryConfig query) throws CouchbaseException;
    
    /**
     * Api to insert the document into underlying couchbase storage.
     * @param query     The query object that encapsulates the bucket, scope, collection, keys, etc.
     * @return Result     List of documents 
     * @throws CouchbaseException 
     */
    public abstract Result insert(CBQueryConfig query) throws CouchbaseException;
    
    /**
     * Api to update the document in the couchbase storage.
     * @param query
     * @return Result
     * @throws CouchbaseException 
     */
    public abstract Result update(CBQueryConfig query) throws CouchbaseException;
    
    /**
     * Api to delete the document from the couchbase storage.
     * @param query
     * @return Result
     * @throws CouchbaseException 
     */
    public abstract Result delete(CBQueryConfig query) throws CouchbaseException;
}
