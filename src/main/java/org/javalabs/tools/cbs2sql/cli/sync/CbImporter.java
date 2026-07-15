package org.javalabs.tools.cbs2sql.cli.sync;

import com.couchbase.client.core.error.CollectionNotFoundException;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.json.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.javalabs.tools.cbs2sql.cli.model.SyncOptions;
import org.javalabs.tools.cbs2sql.cli.storage.Status;
import org.javalabs.tools.cbs2sql.cli.storage.Storage;
import org.javalabs.tools.cbs2sql.cli.storage.impl.CBQueryConfig;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;
import org.javalabs.tools.cbs2sql.cli.util.StopWatch;

/**
 *
 * @author schan280
 */
public class CbImporter {

    /**
     * Exports documents from the specified bucket, scope, and collection to one or more JSON files.
     * 
     * <p>
     * Documents are read from the source storage in batches and written to the specified output directory. The export
     * can be limited to a maximum number of documents and may optionally produce verbose output.
     *
     * @param dest      The source {@link Storage} instance from which data is exported.
     * @param file      The file containing the document set.
     * @param dataset   The bucket name containing the data to export.
     * @param table     The scope and collection identifier of the source data.
     * @param opts      The import options.
     * 
     * @throws IOException  if an error occurs while creating or writing the export files
     */
    public void _import(Storage dest
            , String dataset
            , File file
            , String table
            , SyncOptions opts) throws IOException {

        StopWatch timer = StopWatch.newTimer();

        if ("Y".equalsIgnoreCase(opts.getVerbose())) {
            ConsoleWriter.println(String.format(
                    "Started to import dataset %s from %s to %s. Size (byte): %,d"
                        , dataset.toLowerCase()
                        , file.getAbsolutePath()
                        , dest.host()
                        , file.length()));
        }
        List<JsonObject> records = new ArrayList<>(opts.getBatchSize());

        // Split the table (`bucket`.`scope`.`collection`) into bucket, scope and collection
        String[] tmp = table.split("`\\.`");
        tmp[0] = tmp[0].replace("`", "");
        tmp[2] = tmp[2].replace("`", "");

        CBQueryConfig query = new CBQueryConfig(tmp[0], tmp[1], tmp[2]);
        query.setBatch(Boolean.TRUE);
        query.setRecords(records);
        Integer idx = 1;
        
        Status status = null;
        BufferedReader br = null;
        
        try {
            timer.start();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            
            for (String line = br.readLine(); line != null; line = br.readLine(), idx ++) {
                JsonObject row = JsonObject.fromJson(line);
                records.add(JsonObject.create()
                        .put("id", row.getString("id"))
                        .put("content", row.getObject("document")));

                if (records.size() == opts.getBatchSize()) {
                    status = dest.insert(query);

                    timer.stop();
                    if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                        ConsoleWriter.println(String.format(
                                "Imported %d record(s). Status: %s. Elapse time(ms): %d"
                                    , idx
                                    , status
                                    , timer.elapsedTimeMillis()));
                    }
                    records.clear();
                    timer.reset();

                    timer.start();
                }
            }
            if (! records.isEmpty()) {
                status = dest.insert(query);

                timer.stop();
                if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                    ConsoleWriter.println(String.format(
                            "Imported %d record(s). Status: %s. Elapse time(ms): %d"
                                , idx
                                , status
                                , timer.elapsedTimeMillis()));
                }
                records.clear();
            }
        }
        catch (CouchbaseException e) {
            if (e instanceof CollectionNotFoundException) {
                if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                    ConsoleWriter.println(String.format("Collection %s is not found. Creating new ...", query.getResource()));
                }
                timer.start();
                query.setDdlCreate(Boolean.TRUE);
                createCollection(dest, query);

                // Insert again
                status = dest.insert(query);
                timer.stop();
                
                if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                    ConsoleWriter.println(String.format(
                            "Imported %d record(s). Status: %s. Elapse time(ms): %d"
                                , idx
                                , status
                                , timer.elapsedTimeMillis()));
                }
                records.clear();
            }
            else {
                throw e;
            }
        }
    }
    
    private void createCollection(Storage storage, CBQueryConfig query) {
        storage.ddlOps(query);
    }
}
