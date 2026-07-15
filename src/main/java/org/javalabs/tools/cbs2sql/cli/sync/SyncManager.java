package org.javalabs.tools.cbs2sql.cli.sync;

import org.javalabs.tools.cbs2sql.cli.storage.Storage;
import org.javalabs.tools.cbs2sql.cli.storage.config.BucketConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.CollectionConfig;
import org.javalabs.tools.cbs2sql.cli.storage.config.ScopeConfig;
import org.javalabs.tools.cbs2sql.cli.storage.impl.CBQueryConfig;
import org.javalabs.tools.cbs2sql.cli.storage.impl.CouchbaseStorage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.model.SyncOptions;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;

/**
 *
 * @author schan280
 */
public class SyncManager {

    private static final SyncManager MANAGER = new SyncManager();
    
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM {0}";
    
    private volatile Boolean running = Boolean.FALSE;
    private volatile Boolean stopFlag = Boolean.FALSE;
    
    private final CbExporter exporter = new CbExporter();
    private final CbImporter importer = new CbImporter();
    
    private SyncManager() {}
    
    public static SyncManager getInstance() {
        return MANAGER;
    }

    public Boolean isRunning() {
        return running;
    }

    public void stopRequested() {
        this.stopFlag = Boolean.TRUE;
    }
    
    public void export(SyncOptions opts) {
        Storage source = null;
        
        try {
            if (isRunning()) {
                return;
            }
            this.running = Boolean.TRUE;
            
            source = CouchbaseStorage.get();
            source.init(opts.getConfig());
            
            // Prepare the final table name. It will be in the form of `bucket`.`scope`.`collection`
            BucketConfig bucketConfig = opts.getConfig().getCluster().getBuckets().get(0);
            String table = "`" + bucketConfig.getName() + "`";
            
            if (bucketConfig.getScopes() != null && ! bucketConfig.getScopes().isEmpty()) {
                ScopeConfig scopeConfig = bucketConfig.getScopes().get(0);
                table += "." + "`" + scopeConfig.getName() + "`";
                
                if (scopeConfig.getCollections() != null && ! scopeConfig.getCollections().isEmpty()) {
                    CollectionConfig collectionConfig = scopeConfig.getCollections().get(0);
                    table += "." + "`" + collectionConfig.getName() + "`";
                }
            }
            if (opts.getDatasets() == null || opts.getDatasets().isEmpty()) {
                // Check if dataset-file is provided.
                if (opts.getDatasetFile() != null) {
                    List<String> lines = Files.readAllLines(Path.of(opts.getDatasetFile()));
                    
                    for (String dataset : lines) {
                        if (stopFlag) {
                            break;
                        }
                        Integer total = recordCount(source, table, opts.getIdxCol(), dataset);
                        if (total == 0) {
                            if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                                ConsoleWriter.println(String.format("No data found for %s in %s", dataset, table));
                            }
                            continue;
                        }
                        exporter._export(source
                                , dataset
                                , table
                                , total
                                , opts);
                    }
                    
                }
                else {
                    // No dataset is provided. Therefore blindly fetch all records from the `bucket`.`scope`.`collection`.
                    Integer total = recordCount(source, table);
                    if (total == 0) {
                        if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                            ConsoleWriter.println(String.format("No data found in %s", table));
                        }
                    }
                    else {
                        exporter._export(source
                                , null
                                , table
                                , total
                                , opts);
                    }
                }
            }
            else {
                for (String dataset : opts.getDatasets()) {
                    if (stopFlag) {
                        break;
                    }
                    Integer total = recordCount(source, table, opts.getIdxCol(), dataset);
                    if (total == 0) {
                        if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                            ConsoleWriter.println(String.format("No data found for %s in %s", dataset, table));
                        }
                        continue;
                    }
                    exporter._export(source
                            , dataset
                            , table
                            , total
                            , opts);
                }
            }
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            if (source != null) {
                source.windUp();
            }
            this.running = Boolean.FALSE;
            this.stopFlag = Boolean.FALSE;
        }
    }
    
    public void imprt(SyncOptions opts) {
        Storage dest = null;
        BufferedReader br = null;
        
        try {
            if (isRunning()) {
                return;
            }
            this.running = Boolean.TRUE;
            
            dest = CouchbaseStorage.get();
            dest.init(opts.getConfig());
            
            // Prepare the final table name. It will be in the form of `bucket`.`scope`.`collection`
            BucketConfig bucketConfig = opts.getConfig().getCluster().getBuckets().get(0);
            String table = "`" + bucketConfig.getName() + "`";
            
            if (bucketConfig.getScopes() != null && ! bucketConfig.getScopes().isEmpty()) {
                ScopeConfig scopeConfig = bucketConfig.getScopes().get(0);
                table += "." + "`" + scopeConfig.getName() + "`";
                
                if (scopeConfig.getCollections() != null && ! scopeConfig.getCollections().isEmpty()) {
                    CollectionConfig collectionConfig = scopeConfig.getCollections().get(0);
                    table += "." + "`" + collectionConfig.getName() + "`";
                }
            }
            
            File inDir = new File(opts.getInDir());
            Map<String, String> datasetFileMap = new HashMap<>();
            
            if (opts.getFilename() == null) {
                // Read and Import all files from the inDir.
                String[] list = inDir.list((File dir, String name) -> {
                    return name.endsWith(opts.getExtn());
                });
                for (String file : list) {
                    datasetFileMap.put(file.substring(0, file.indexOf(opts.getExtn())), opts.getInDir() + File.separator + file);
                }
                if (opts.getExcludes() != null && ! opts.getExcludes().isEmpty()) {
                    for (String exclude : opts.getExcludes()) {
                        datasetFileMap.remove(exclude);
                    }
                }
            }
            else {
                // Read and Import only specific file (or dataset) from the inDir.
                datasetFileMap.put(opts.getFilename().substring(0, opts.getFilename().indexOf(opts.getExtn())), opts.getInDir() + File.separator + opts.getFilename());
            }
            
            for (Map.Entry<String, String> me : datasetFileMap.entrySet()) {
                if (stopFlag) {
                    break;
                }
                importer._import(dest, me.getKey(), new File(me.getValue()), table, opts);
            }
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                // Do nothing
            }
            if (dest != null) {
                dest.windUp();
            }
            this.running = Boolean.FALSE;
            this.stopFlag = Boolean.FALSE;
        }
    }
    
    private Integer recordCount(Storage source, String table) {
        return recordCount(source, table, null, null);
    }
    
    private Integer recordCount(Storage source, String table, String idxCol, String dataset) {
        CBQueryConfig fQuery = new CBQueryConfig();
        
        // 1. Get the total number of records for this dataset in source db
        String query = MessageFormat.format(COUNT_QUERY, table);
        if (idxCol != null && dataset != null) {
            query += " WHERE " + idxCol + " = $" + idxCol;
            fQuery.setParameters(Map.of(idxCol, dataset));
        }
        fQuery.setN1ql(query);
        fQuery.setBatch(Boolean.TRUE);
        
        List<Map> rows = source.select(fQuery);
        return (Integer)rows.get(0).get("$1");
    }
}
