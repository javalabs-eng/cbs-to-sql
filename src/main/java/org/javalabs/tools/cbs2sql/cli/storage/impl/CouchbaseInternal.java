package org.javalabs.tools.cbs2sql.cli.storage.impl;

import com.couchbase.client.core.deps.org.LatencyUtils.LatencyStats;
import com.couchbase.client.core.deps.org.LatencyUtils.PauseDetector;
import com.couchbase.client.core.deps.org.LatencyUtils.SimplePauseDetector;
import org.javalabs.tools.cbs2sql.cli.storage.Storage;
import org.javalabs.tools.cbs2sql.cli.storage.config.BucketConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.CollectionConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.CouchbaseConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.ScopeConfig;
import org.javalabs.tools.cbs2sql.cli.util.StopWatch;
import com.couchbase.client.core.diagnostics.PingResult;
import com.couchbase.client.core.env.ThresholdLoggingTracerConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author schan280
 */
public abstract class CouchbaseInternal implements Storage {

    protected static final String CONNECT_STRING = "couchbase://{0}";

    private CouchbaseConfig cbConfig;
    private ClusterEnvironment sharedEnv;
    private Cluster cluster = null;

    private volatile Boolean initialized = Boolean.FALSE;

    private final QueryStrategy keyedStrategy;
    private final QueryStrategy batchStrategy;

    protected CouchbaseInternal() {
        keyedStrategy = new KeyedQueryStrategy(this);
        batchStrategy = new BatchQueryStrategy(this);
    }

    @Override
    public synchronized void init(String config) {
        if (initialized) {
            return;
        }
        try {
            StopWatch timer = StopWatch.newTimer();
            timer.start();

            File file = new File(config);
            InputStream iStream = null;

            if (!file.exists()) {
                iStream = getClass().getClassLoader().getResourceAsStream(config);
                if (iStream == null || iStream.available() == 0) {
                    URL fileURL = getClass().getClassLoader().getResource(config);
                    String fname = fileURL.getFile();
                    fname = fname.replace('\\', '/');
                    file = new File(fname);
                    if (!file.exists()) {
                        throw new IOException("Configuration File Not Found... Exiting from the System !!!!");
                    }
                    iStream = new FileInputStream(file);
                }
            } else {
                iStream = new FileInputStream(file);
            }
            this.cbConfig = new ObjectMapper().readValue(iStream, CouchbaseConfig.class);
            ConsoleWriter.println(String.format("Loaded couchbase configuration: %s", cbConfig));

            init(cbConfig);

            timer.stop();
            ConsoleWriter.println(String.format("Initialized cluster %s and their %s buckets. Elapsed time(ms): %d",
                     cbConfig.getCluster().getName(),
                     cbConfig.getCluster().getBuckets().size(),
                     timer.elapsedTimeMillis()));
            initialized = Boolean.TRUE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void init(CouchbaseConfig cbConfig) {
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        this.cbConfig = cbConfig;
        Integer timeOut = (Integer) cbConfig.getParams().getOrDefault("timeoutMs", 5000);

        PauseDetector detector = new SimplePauseDetector(
                1_000_000L,             // sleepInterval
                1_000_000L,             // pauseNotificationThreshold
                1                       // numberOfDetectorThreads
        );
        LatencyStats.setDefaultPauseDetector(detector);

        Scheduler scheduler = Schedulers.newParallel("cb-parallel", 2);
        sharedEnv = ClusterEnvironment.builder()
                .thresholdLoggingTracerConfig((ThresholdLoggingTracerConfig.Builder t) -> {
                    t.queryThreshold(Duration.ofMinutes(1))
                            .emitInterval(Duration.ofMinutes(2));
                })
                .timeoutConfig(timeout -> timeout.kvTimeout(Duration.ofSeconds(timeOut)))
                .scheduler(scheduler)
                .schedulerThreadCount(2) // -Dreactor.schedulers.defaultPoolSize=4
                .ioConfig(io -> {
                    io
                            .maxHttpConnections(2)
                            .numKvConnections(2);
                })
                .build();

        this.cluster = Cluster.connect(
                MessageFormat.format(CONNECT_STRING, cbConfig.getHost()),
                ClusterOptions.clusterOptions(cbConfig.getCluster().getUser(), cbConfig.getCluster().getPassword()).environment(sharedEnv));

        for (BucketConfig bcConfig : cbConfig.getCluster().getBuckets()) {
            cluster.bucket(bcConfig.getName());
        }
        if ("Y".equalsIgnoreCase(cbConfig.getVerbose())) {
            ConsoleWriter.println(String.format("Connected to cluster %s. Host: %s", cbConfig.getCluster().getName(), cbConfig.getHost()));
        }
    }

    public CouchbaseConfig config() {
        return cbConfig;
    }

    public Cluster cluster() {
        return cluster;
    }

    protected QueryStrategy strategy(Boolean batch) {
        return batch ? batchStrategy : keyedStrategy;
    }

    @Override
    public String host() {
        return cbConfig.getHost();
    }

    @Override
    public void ping() {
        BucketManager bucketManager = cluster.buckets();
        for (String bucketName : bucketManager.getAllBuckets().keySet()) {
            Bucket bucket = cluster.bucket(bucketName);
            PingResult result = bucket.ping();
            ConsoleWriter.println(String.format("Pinged bucket %s. Result => Id: %s. Sdk: %s. Version: %s",
                    bucketName, result.id(), result.sdk(), result.version()));
        }
    }

    protected String getBucket(String bucketName) {
        if (bucketName != null) {
            return bucketName;
        }
        for (BucketConfig bkConfig : cbConfig.getCluster().getBuckets()) {
            if (bkConfig.getDefault()) {
                return bkConfig.getName();
            }
        }
        return null;
    }

    protected String getScope(String bucketName, String scopeName) {
        if (scopeName != null) {
            return scopeName;
        }
        for (BucketConfig bkConfig : cbConfig.getCluster().getBuckets()) {
            if (bucketName.equals(bkConfig.getName())) {
                for (ScopeConfig scConfig : bkConfig.getScopes()) {
                    if (scConfig.getDefault()) {
                        return scConfig.getName();
                    }
                }
            }
        }
        return "_default";
    }

    protected String getCollection(String bucketName, String scopeName, String collectionName) {
        if (collectionName != null) {
            return collectionName;
        }
        for (BucketConfig bkConfig : cbConfig.getCluster().getBuckets()) {
            if (bucketName.equals(bkConfig.getName())) {
                for (ScopeConfig scConfig : bkConfig.getScopes()) {
                    if (scConfig.getName().equals(scopeName)) {
                        for (CollectionConfig ccConfig : scConfig.getCollections()) {
                            if (ccConfig.getDefault()) {
                                return ccConfig.getName();
                            }
                        }
                    }
                }
            }
        }
        return "_default";
    }

    @Override
    public void windUp() {
        if (cluster != null) {
            cluster.disconnect();
            if ("Y".equalsIgnoreCase(cbConfig.getVerbose())) {
                ConsoleWriter.println(String.format("Disconnected from cluster %s", cbConfig.getCluster().getName()));
            }
        }
        if (sharedEnv != null) {
            sharedEnv.shutdown();
        }
    }

}
