package org.javalabs.tools.cbs2sql.cli.storage.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the configuration required to connect to a Couchbase cluster.
 *
 * <p>This class encapsulates the connection details, cluster configuration,
 * optional connection parameters, and logging preferences used to initialize
 * a Couchbase storage implementation.</p>
 *
 * <p>The configuration typically includes the Couchbase host and port, one or
 * more buckets defined in the associated {@link ClusterConfig}, and any
 * implementation-specific connection parameters.</p>
 *
 * @author schan280
 */
public class CouchbaseConfig {
    
    private String host;
    private Integer port;
    private Map<String, Object> params = new HashMap<>();
    
    private ClusterConfig cluster;
    private String verbose = "N";

    /**
     * Returns the hostname or IP address of the Couchbase server.
     *
     * @return the Couchbase host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the hostname or IP address of the Couchbase server.
     *
     * @param host the Couchbase host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the port used to connect to the Couchbase server.
     *
     * @return the Couchbase port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the port used to connect to the Couchbase server.
     *
     * @param port the Couchbase port
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Returns the implementation-specific connection parameters.
     *
     * <p>The returned map may contain additional configuration values required
     * by the underlying Couchbase SDK or storage implementation.</p>
     *
     * @return a map of connection parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Sets the implementation-specific connection parameters.
     *
     * @param params the connection parameters
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * Returns the Couchbase cluster configuration.
     *
     * @return the cluster configuration
     */
    public ClusterConfig getCluster() {
        return cluster;
    }

    /**
     * Sets the Couchbase cluster configuration.
     *
     * @param cluster the cluster configuration
     */
    public void setCluster(ClusterConfig cluster) {
        this.cluster = cluster;
    }

    /**
     * Returns whether verbose logging is enabled.
     *
     * <p>The value is expected to be {@code "Y"} to enable verbose logging or
     * {@code "N"} to disable it.</p>
     *
     * @return the verbose logging flag
     */
    public String getVerbose() {
        return verbose;
    }

    /**
     * Sets whether verbose logging should be enabled.
     *
     * <p>The expected values are {@code "Y"} and {@code "N"}.</p>
     *
     * @param verbose the verbose logging flag
     */
    public void setVerbose(String verbose) {
        this.verbose = verbose;
    }
}
