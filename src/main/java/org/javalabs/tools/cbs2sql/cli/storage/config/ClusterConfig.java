package org.javalabs.tools.cbs2sql.cli.storage.config;

import java.util.List;

/**
 *
 * @author schan280
 */
public class ClusterConfig {
    
    private String name;
    private String user;
    private String password;
    private Boolean _default;
    
    private List<BucketConfig> buckets;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getDefault() {
        return _default;
    }

    public void setDefault(Boolean _default) {
        this._default = _default;
    }

    public List<BucketConfig> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<BucketConfig> buckets) {
        this.buckets = buckets;
    }
}
