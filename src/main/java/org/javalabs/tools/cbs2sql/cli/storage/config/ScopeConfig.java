package org.javalabs.tools.cbs2sql.cli.storage.config;

import java.util.List;

/**
 * Represents the configuration of a Couchbase scope.
 *
 * <p>A scope is a logical namespace within a Couchbase bucket and groups one
 * or more collections. This class stores the scope name, whether it is the
 * default scope, and the collections it contains.</p>
 *
 * <p>By default, a newly created instance contains the Couchbase default
 * collection ({@code _default}).</p>
 *
 * @author schan280
 */
public class ScopeConfig {
    
    /**
     * Name of the Couchbase default scope.
     */
    public static final String DEFAULT_SCOPE = "_default";
    
    private String name;
    private Boolean _default;
    private List<CollectionConfig> collections;
    
    public ScopeConfig() {
        collections = List.of(new CollectionConfig(CollectionConfig.DEFAULT_COLLECTION));
    }
    
    public ScopeConfig(String name) {
        this();
        this.name = name;
    }

    /**
     * Returns the scope name.
     *
     * @return the scope name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the scope name.
     *
     * @param name the scope name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Indicates whether this is the default Couchbase scope.
     *
     * @return {@code true} if this is the default scope; otherwise
     *         {@code false}
     */
    public Boolean getDefault() {
        return _default;
    }

    /**
     * Sets whether this represents the default Couchbase scope.
     *
     * @param _default {@code true} if this is the default scope
     */
    public void setDefault(Boolean _default) {
        this._default = _default;
    }

    /**
     * Returns the collections defined for this scope.
     *
     * @return the configured collections
     */
    public List<CollectionConfig> getCollections() {
        return collections;
    }

    /**
     * Sets the collections defined for this scope.
     *
     * @param collections the collections to associate with this scope
     */
    public void setCollections(List<CollectionConfig> collections) {
        this.collections = collections;
    }
}
