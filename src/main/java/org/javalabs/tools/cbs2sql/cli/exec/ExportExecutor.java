package org.javalabs.tools.cbs2sql.cli.exec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.javalabs.tools.cbs2sql.cli.main.ExecutorBase;
import org.javalabs.tools.cbs2sql.cli.model.SyncOptions;
import org.javalabs.tools.cbs2sql.cli.storage.config.BucketConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.ClusterConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.CollectionConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.CouchbaseConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.ScopeConfig;
import org.javalabs.tools.cbs2sql.cli.sync.SyncManager;

/**
 * Schema generator.
 * 
 * This executor will connect to remote couchbase db and bucket and generate the sql schemas for all collection.
 * 
 * <p>
 * However, if you have one bucket, and the documents within that bucket are distinguished based on certain attribute(s)
 * for each such document type it will generate the associated sql schema.
 *
 * @author Sudiptasish Chanda
 */
public class ExportExecutor implements ExecutorBase {
    
    private final String name = "cb-export";
    private final String description = "Export the document and dump it to a local file";
    private final List<String> longOptions = Arrays.asList("--cluster", "--host", "--port", "--user", "--password", "--bucket", "--json", "--verbose", "--out-dir", "--in-dir", "-extn", "--file-name", "--scope", "--collection", "--data-set", "--data-set-file", "--index-column", "--exclude");
    private final List<String> shortOptions = Arrays.asList("-c", "-h", "-p", "-u", "-w", "-b", "-j", "-v", "-o", "-i", "-e", "-f", "-s", "-l", "-d", "-t", "-n", "-x");
    
    public ExportExecutor() {}

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }
    
    @Override
    public void start(String[] options) throws Exception {
        SyncOptions opts = prepare(options);
        SyncManager.getInstance().export(opts);
    }
    
    private SyncOptions prepare(String[] options) throws IOException {
        BucketConfig bcConfig = new BucketConfig();
        
        ClusterConfig clConfig = new ClusterConfig();
        clConfig.setBuckets(List.of(bcConfig));
        
        CouchbaseConfig cbConfig = new CouchbaseConfig();
        cbConfig.setCluster(clConfig);
        
        SyncOptions opts = new SyncOptions();
        opts.setConfig(cbConfig);
        
        for (int i = 0; i < options.length; i ++) {
            if (options[i].equals("-c") || options[i].equals("--cluster")) {
                verifyArg(options[i], options[i + 1]);
                clConfig.setName(options[i + 1]);
            }
            else if (options[i].equals("-u") || options[i].equals("--user")) {
                verifyArg(options[i], options[i + 1]);
                clConfig.setUser(options[i + 1]);
            }
            else if (options[i].equals("-w") || options[i].equals("--password")) {
                verifyArg(options[i], options[i + 1]);
                clConfig.setPassword(options[i + 1]);
            }
            else if (options[i].equals("-h") || options[i].equals("--host")) {
                verifyArg(options[i], options[i + 1]);
                cbConfig.setHost(options[i + 1]);
            }
            else if (options[i].equals("-p") || options[i].equals("--port")) {
                verifyArg(options[i], options[i + 1]);
                cbConfig.setPort(Integer.valueOf(options[i + 1]));
            }
            else if (options[i].equals("-b") || options[i].equals("--bucket")) {
                verifyArg(options[i], options[i + 1]);
                bcConfig.setName(options[i + 1]);
            }
            else if (options[i].equals("-s") || options[i].equals("--scope")) {
                verifyArg(options[i], options[i + 1]);
                ScopeConfig scConfig = new ScopeConfig();
                scConfig.setName(options[i + 1]);
                
                bcConfig.setScopes(List.of(scConfig));
            }
            else if (options[i].equals("-l") || options[i].equals("--collection")) {
                verifyArg(options[i], options[i + 1]);
                CollectionConfig ccConfig = new CollectionConfig();
                ccConfig.setName(options[i + 1]);
                
                bcConfig.getScopes().get(0).setCollections(List.of(ccConfig));
            }
            else if (options[i].equals("-o") || options[i].equals("--out-dir")) {
                verifyArg(options[i], options[i + 1]);
                opts.setOutDir(options[i + 1]);
            }
            else if (options[i].equals("-i") || options[i].equals("--in-dir")) {
                verifyArg(options[i], options[i + 1]);
                opts.setInDir(options[i + 1]);
            }
            else if (options[i].equals("-e") || options[i].equals("--extn")) {
                verifyArg(options[i], options[i + 1]);
                opts.setExtn(options[i + 1]);
            }
            else if (options[i].equals("-f") || options[i].equals("--file-name")) {
                verifyArg(options[i], options[i + 1]);
                opts.setFilename(options[i + 1]);
            }
            else if (options[i].equals("-d") || options[i].equals("--data-set")) {
                verifyArg(options[i], options[i + 1]);
                List<String> dataset = new ArrayList<>();
                
                // Check if this is a file where all the datasets are specified.
                if (options[i + 1].contains("/")) {
                    // It must be a newline separated file, i.e., every line in the file represents a dataset.
                    dataset = Files.readAllLines(Path.of(options[i + 1]));
                }
                else {
                    for (String ds : options[i + 1].split(",")) {
                        dataset.add(ds.trim());
                    }
                }
                opts.setDatasets(dataset);
            }
            else if (options[i].equals("-t") || options[i].equals("--data-set-file")) {
                verifyArg(options[i], options[i + 1]);
                opts.setDatasetFile(options[i + 1]);
            }
            else if (options[i].equals("-n") || options[i].equals("--index-column")) {
                verifyArg(options[i], options[i + 1]);
                opts.setIdxCol(options[i + 1]);
            }
            else if (options[i].equals("-x") || options[i].equals("--exclude")) {
                verifyArg(options[i], options[i + 1]);
                List<String> excludes = new ArrayList<>();
                for (String ex : options[i + 1].split(",")) {
                    excludes.add(ex.trim());
                }
                opts.setExcludes(excludes);
            }
            else if (options[i].equals("-v") || options[i].equals("--verbose")) {
                verifyArg(options[i], options[i + 1]);
                opts.setVerbose(options[i + 1]);
            }
        }
        cbConfig.setVerbose(opts.getVerbose());
        return opts;
    }
    
    private void verifyArg(String param, String val) {
        if (longOptions.contains(val) || shortOptions.contains(val)) {
            throw new IllegalArgumentException("Missing value for option [" + param + "]");
        }
    }
}
