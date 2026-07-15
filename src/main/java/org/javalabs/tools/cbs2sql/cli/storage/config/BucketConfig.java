package org.javalabs.tools.cbs2sql.cli.storage.config;

import java.util.List;

/**
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

    public List<ScopeConfig> getScopes() {
        return scopes;
    }

    public void setScopes(List<ScopeConfig> scopes) {
        this.scopes = scopes;
    }
}
