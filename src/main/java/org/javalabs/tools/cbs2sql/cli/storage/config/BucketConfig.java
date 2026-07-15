package org.javalabs.tools.cbs2sql.cli.storage.config;

import java.util.List;

/**
 * Represents the configuration of a Couchbase bucket.
 *
 * <p>A bucket is the top-level container in a Couchbase cluster. It contains
 * one or more scopes, each of which contains one or more collections. This
 * class stores the bucket name, whether it is the default bucket, and the
 * configured scopes.</p>
 *
 * <p>By default, a newly created instance contains the Couchbase default
 * scope ({@code _default}).</p>
 *
 * @author schan280
 */
public class BucketConfig {
    
    private String name;
    private Boolean _default;
    private List<ScopeConfig> scopes;
    
    public BucketConfig() {
        scopes = List.of(new ScopeConfig(ScopeConfig.DEFAULT_SCOPE));
    }
    
    public BucketConfig(String name) {
        this();
        this.name = name;
    }

    /**
     * Returns the bucket name.
     *
     * @return the bucket name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the bucket name.
     *
     * @param name the bucket name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Indicates whether this is the default Couchbase bucket.
     *
     * @return {@code true} if this is the default bucket; otherwise
     *         {@code false}
     */
    public Boolean getDefault() {
        return _default;
    }

    /**
     * Sets whether this represents the default Couchbase bucket.
     *
     * @param _default {@code true} if this is the default bucket
     */
    public void setDefault(Boolean _default) {
        this._default = _default;
    }

    /**
     * Returns the scopes defined for this bucket.
     *
     * @return the configured scopes
     */
    public List<ScopeConfig> getScopes() {
        return scopes;
    }

    /**
     * Sets the scopes defined for this bucket.
     *
     * @param scopes the scopes to associate with this bucket
     */
    public void setScopes(List<ScopeConfig> scopes) {
        this.scopes = scopes;
    }
}
