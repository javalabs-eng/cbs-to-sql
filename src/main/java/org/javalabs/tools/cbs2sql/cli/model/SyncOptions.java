package org.javalabs.tools.cbs2sql.cli.model;

import org.javalabs.tools.cbs2sql.cli.storage.config.CouchbaseConfig;
import java.util.List;

/**
 *
 * @author schan280
 */
public class SyncOptions {
 
    private  String datasetFile;
    private  List<String> datasets;
    private  String idxCol = "type";
    private  String filename;
    private  Integer batchSize = 2000;
    private  List<String> excludes;
    private  String outDir;
    private  String inDir;
    private  String extn = ".json";
    private  String verbose = "N";
    
    private CouchbaseConfig config;

    public String getDatasetFile() {
        return datasetFile;
    }

    public void setDatasetFile(String datasetFile) {
        this.datasetFile = datasetFile;
    }

    public List<String> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<String> datasets) {
        this.datasets = datasets;
    }

    public String getIdxCol() {
        return idxCol;
    }

    public void setIdxCol(String idxCol) {
        this.idxCol = idxCol;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public CouchbaseConfig getConfig() {
        return config;
    }

    public void setConfig(CouchbaseConfig config) {
        this.config = config;
    }

    public String getInDir() {
        return inDir;
    }

    public void setInDir(String inDir) {
        this.inDir = inDir;
    }

    public String getOutDir() {
        return outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    public String getExtn() {
        return extn;
    }

    public void setExtn(String extn) {
        this.extn = extn;
    }

    public String getVerbose() {
        return verbose;
    }

    public void setVerbose(String verbose) {
        this.verbose = verbose;
    }
}
