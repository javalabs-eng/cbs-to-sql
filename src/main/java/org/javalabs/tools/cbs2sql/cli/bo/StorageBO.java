package org.javalabs.tools.cbs2sql.cli.bo;

import org.javalabs.tools.cbs2sql.cli.model.Document;
import org.javalabs.tools.cbs2sql.cli.storage.Status;
import org.javalabs.tools.cbs2sql.cli.storage.Storage;
import org.javalabs.tools.cbs2sql.cli.storage.impl.CBQueryConfig;
import org.javalabs.tools.cbs2sql.cli.storage.impl.CouchbaseStorage;
import org.javalabs.tools.cbs2sql.cli.util.StopWatch;
import com.couchbase.client.java.json.JsonObject;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;

/**
 * Business object for storage data manipulation.
 *
 * @author schan280
 */
public class StorageBO {
    
    private final Storage storage;
    
    public StorageBO() {
        this.storage = CouchbaseStorage.get();
        this.storage.init("couchbase.json");
    }
    
    public List<Map> get(String collection, String key) {
        try {
            StopWatch timer = StopWatch.newTimer();
            timer.start();

            CBQueryConfig query = new CBQueryConfig(collection);
            query.setKey(key);

            List<Map> results = storage.select(query);
            timer.stop();
            ConsoleWriter.println(String.format("Fetched %d document(s). Elapsed time(ms): %d", results.size(), timer.elapsedTimeMillis()));
            
            return results;
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public Status add(String collection, Document doc) {
        try {
            StopWatch timer = StopWatch.newTimer();
            timer.start();

            CBQueryConfig query = new CBQueryConfig(collection);
            query.setRecords(List.of(JsonObject.create().put("id", doc.getId()).put("content", doc.getContent())));

            Status status = storage.insert(query);
            ConsoleWriter.println(String.format("Added %d document(s). Status: %s. Elapsed time(ms): %d", 1, status, timer.elapsedTimeMillis()));
            
            return status;
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public Status update(String collection, String key, Document doc) {
        try {
            StopWatch timer = StopWatch.newTimer();
            timer.start();

            CBQueryConfig query = new CBQueryConfig(collection);
            query.setRecords(List.of(JsonObject.create().put("id", key).put("content", doc.getContent())));

            Status status = storage.update(query);
            ConsoleWriter.println(String.format("Updated %d document(s). Status: %s. Elapsed time(ms): %d", 1, status, timer.elapsedTimeMillis()));
            
            return status;
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public Status delete(String collection, String key) {
        try {
            StopWatch timer = StopWatch.newTimer();
            timer.start();

            CBQueryConfig query = new CBQueryConfig(collection);
            query.setKey(key);

            Status status = storage.delete(query);
            ConsoleWriter.println(String.format("Deleted %d document(s). Status: %s. Elapsed time(ms): %d", 1, status, timer.elapsedTimeMillis()));
            
            return status;
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
