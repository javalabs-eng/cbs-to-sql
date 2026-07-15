package org.javalabs.tools.cbs2sql.cli.storage.config;

import java.util.List;

/**
 * Represents the configuration of a Couchbase cluster.
 *
 * <p>A cluster is the top-level configuration entity used to establish a
 * connection to a Couchbase deployment. It contains the authentication
 * credentials and the list of buckets that are available for import,
 * export, or query operations.</p>
 *
 * <p>This class is typically used as part of a
 * {@link CouchbaseConfig} instance to describe the complete Couchbase
 * connection hierarchy.</p>
 *
 * @author schan280
 */
public class ClusterConfig {
    
    private String name;
    private String user;
    private String password;
    private Boolean _default;
    
    private List<BucketConfig> buckets;
    
    /**
     * Returns the cluster name.
     *
     * @return the cluster name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the cluster name.
     *
     * @param name the cluster name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the username used to authenticate with the Couchbase cluster.
     *
     * @return the cluster username
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the username used to authenticate with the Couchbase cluster.
     *
     * @param user the cluster username
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Returns the password used to authenticate with the Couchbase cluster.
     *
     * @return the cluster password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password used to authenticate with the Couchbase cluster.
     *
     * @param password the cluster password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Indicates whether this is the default Couchbase cluster.
     *
     * @return {@code true} if this is the default cluster; otherwise
     *         {@code false}
     */
    public Boolean getDefault() {
        return _default;
    }

    /**
     * Sets whether this represents the default Couchbase cluster.
     *
     * @param _default {@code true} if this is the default cluster
     */
    public void setDefault(Boolean _default) {
        this._default = _default;
    }

    /**
     * Returns the buckets configured for this Couchbase cluster.
     *
     * @return the configured bucket definitions
     */
    public List<BucketConfig> getBuckets() {
        return buckets;
    }

    /**
     * Sets the buckets configured for this Couchbase cluster.
     *
     * @param buckets the bucket definitions
     */
    public void setBuckets(List<BucketConfig> buckets) {
        this.buckets = buckets;
    }
}
