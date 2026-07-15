package org.javalabs.tools.cbs2sql.cli.storage.config;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author schan280
 */
public class CouchbaseConfig {
    
    private String host;
    private Integer port;
    private Map<String, Object> params = new HashMap<>();
    
    private ClusterConfig cluster;
    private String verbose = "N";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public ClusterConfig getCluster() {
        return cluster;
    }

    public void setCluster(ClusterConfig cluster) {
        this.cluster = cluster;
    }

    public String getVerbose() {
        return verbose;
    }

    public void setVerbose(String verbose) {
        this.verbose = verbose;
    }
}
