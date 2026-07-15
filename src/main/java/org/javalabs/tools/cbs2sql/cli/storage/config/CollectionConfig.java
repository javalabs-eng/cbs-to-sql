package org.javalabs.tools.cbs2sql.cli.storage.config;

/**
 * Represents the configuration of a Couchbase collection.
 *
 * <p>A collection is the lowest level in the Couchbase data hierarchy and is
 * used to store documents within a scope. This class stores the collection
 * name and indicates whether it represents the default collection.</p>
 *
 * @author schan280
 */
public class CollectionConfig {
    
    /**
     * Name of the Couchbase default collection.
     */
    public static final String DEFAULT_COLLECTION = "_default";
    
    private String name;
    private Boolean _default;

    public CollectionConfig() {}
    
    public CollectionConfig(String name) {
        this.name = name;
    }

    /**
     * Returns the collection name.
     *
     * @return the collection name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the collection name.
     *
     * @param name the collection name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Indicates whether this is the default Couchbase collection.
     *
     * @return {@code true} if this is the default collection; otherwise
     *         {@code false}
     */
    public Boolean getDefault() {
        return _default;
    }

    /**
     * Sets whether this represents the default Couchbase collection.
     *
     * @param _default {@code true} if this is the default collection
     */
    public void setDefault(Boolean _default) {
        this._default = _default;
    }
}
