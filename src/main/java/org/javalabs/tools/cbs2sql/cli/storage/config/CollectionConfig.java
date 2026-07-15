package org.javalabs.tools.cbs2sql.cli.storage.config;

/**
 *
 * @author schan280
 */
public class CollectionConfig {
    
    public static final String DEFAULT_COLLECTION = "_default";
    
    private String name;
    private Boolean _default;

    public CollectionConfig() {}
    
    public CollectionConfig(String name) {
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
}
