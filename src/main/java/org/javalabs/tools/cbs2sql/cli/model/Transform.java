package org.javalabs.tools.cbs2sql.cli.model;

import java.util.LinkedHashMap;

/**
 *
 * @author schan280
 */
public class Transform {

    private String dataset;
    private String directory;
    private String filename;
    private String flatten = "N";
    private String verbose = "Y";
    private String data = "Y";
    private String dump = "Y";
    private String outDir;
    
    private LinkedHashMap<String, Object> document;

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFlatten() {
        return flatten;
    }

    public void setFlatten(String flatten) {
        this.flatten = flatten;
    }

    public LinkedHashMap<String, Object> getDocument() {
        return document;
    }

    public void setDocument(LinkedHashMap<String, Object> document) {
        this.document = document;
    }

    public String getVerbose() {
        return verbose;
    }

    public void setVerbose(String verbose) {
        this.verbose = verbose;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDump() {
        return dump;
    }

    public void setDump(String dump) {
        this.dump = dump;
    }

    public String getOutDir() {
        return outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }
}
