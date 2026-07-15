package org.javalabs.tools.cbs2sql.cli.storage.config;

import java.util.List;

/**
 *
 * @author schan280
 */
public class ScopeConfig {
    
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDefault() {
        return _default;
    }

    public void setDefault(Boolean _default) {
        this._default = _default;
    }

    public List<CollectionConfig> getCollections() {
        return collections;
    }

    public void setCollections(List<CollectionConfig> collections) {
        this.collections = collections;
    }
}
