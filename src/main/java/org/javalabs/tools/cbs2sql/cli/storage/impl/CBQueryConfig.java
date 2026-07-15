package org.javalabs.tools.cbs2sql.cli.storage.impl;

import org.javalabs.tools.cbs2sql.cli.storage.QueryConfig;
import com.couchbase.client.java.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Couchbase query configuration.
 *
 * @author schan280
 */
public class CBQueryConfig implements QueryConfig {
    
    private String bucket;
    private String scope;
    private String collection;
    private String n1ql;
    private Boolean batch = Boolean.FALSE;
    private Boolean raw = Boolean.FALSE;
    
    private Map<String, Object> params = new HashMap<>();
    
    private List<JsonObject> records = new ArrayList<>();

    private String key;
    
    private Boolean ddlCreate;
    
    public CBQueryConfig() {}
    
    public CBQueryConfig(String collection) {
        this.collection = collection;
    }

    public CBQueryConfig(String bucket, String scope, String collection) {
        this.bucket = bucket;
        this.scope = scope;
        this.collection = collection;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getBucket() {
        return bucket;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    @Override
    public String getResource() {
        return collection;
    }

    public void setN1ql(String n1ql) {
        this.n1ql = n1ql;
    }

    public String getN1ql() {
        return n1ql;
    }

    public void setBatch(Boolean batch) {
        this.batch = batch;
    }

    public Boolean getBatch() {
        return batch;
    }

    public void setParameters(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public Map<String, Object> getParameters() {
        return params;
    }

    public void setRecords(List<JsonObject> records) {
        this.records = records;
    }

    @Override
    public List<JsonObject> getRecords() {
        return records;
    }

    public Boolean getRaw() {
        return raw;
    }

    public void setRaw(Boolean raw) {
        this.raw = raw;
    }

    public Boolean getDdlCreate() {
        return ddlCreate;
    }

    public void setDdlCreate(Boolean ddlCreate) {
        this.ddlCreate = ddlCreate;
    }

    @Override
    public Boolean isVerbose() {
        return Boolean.FALSE;
    }
}
