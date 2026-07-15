package org.javalabs.tools.cbs2sql.cli.exec;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.javalabs.tools.cbs2sql.cli.main.ExecutorBase;
import org.javalabs.tools.cbs2sql.cli.model.Transform;
import org.javalabs.tools.cbs2sql.cli.rel.Introspector;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;

/**
 * Executes database schema definition scripts against the configured target
 * database.
 *
 * <p>{@code SchemaExecutor} is responsible for reading schema definition
 * statements (DDL), executing them in the appropriate order, and ensuring
 * that the target database structure is prepared before data import or
 * synchronization operations begin. Typical operations include creating
 * schemas, tables, indexes, constraints, and other database objects.</p>
 *
 * <p>The executor delegates statement execution to the configured
 * {@code Storage} implementation, allowing the same workflow to support
 * different database providers while remaining independent of the underlying
 * SQL dialect.</p>
 *
 * <p>This class is intended for internal use by the schema migration and
 * synchronization framework and is not designed to be used directly by
 * client applications.</p>
 *
 * @author schan280
 */
public class SchemaExecutor implements ExecutorBase {
    
    private final String name = "gen-schema";
    private final String description = "Generate the sql schema from couchbase doc";
    private final List<String> longOptions = Arrays.asList("--data-set", "--in-dir", "--file-name", "--flatten-child", "--verbose");
    private final List<String> shortOptions = Arrays.asList("-d", "-i", "-f", "-l", "-v");
    
    public SchemaExecutor() {}

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
        Transform payload = prepare(options);
        
        final Introspector introspector = new Introspector();
        String sql = introspector.introspectAndGenerateScript(payload);
        if ("Y".equalsIgnoreCase(payload.getVerbose())) {
            ConsoleWriter.println("\n" + sql);
        }
    }
    
    private Transform prepare(String[] options) throws IOException {
        Transform payload = new Transform();
        
        for (int i = 0; i < options.length; i ++) {
            if (options[i].equals("-d") || options[i].equals("--data-set")) {
                verifyArg(options[i], options[i + 1]);
                payload.setDataset(options[i + 1]);
            }
            else if (options[i].equals("-i") || options[i].equals("--in-dir")) {
                verifyArg(options[i], options[i + 1]);
                payload.setDirectory(options[i + 1]);
            }
            else if (options[i].equals("-f") || options[i].equals("--file-name")) {
                verifyArg(options[i], options[i + 1]);
                payload.setFilename(options[i + 1]);
            }
            else if (options[i].equals("-l") || options[i].equals("--flatten-child")) {
                verifyArg(options[i], options[i + 1]);
                payload.setFlatten(options[i + 1]);
            }
            else if (options[i].equals("-v") || options[i].equals("--verbose")) {
                verifyArg(options[i], options[i + 1]);
                payload.setVerbose(options[i + 1]);
            }
        }
        if (payload.getDataset() == null) {
            if (payload.getFilename().charAt(0) == '*' && payload.getFilename().charAt(1) == '.') {
                // Cannot obtain dataset name.
                payload.setDataset("_all_");
            }
            else {
                int fromIdx = payload.getFilename().lastIndexOf("/");
                if (fromIdx == -1) {
                    fromIdx = 0;
                }
                else {
                    fromIdx += 1;
                }
                int toIdx = payload.getFilename().lastIndexOf(".");
                if (toIdx == -1) {
                    toIdx = payload.getFilename().length();
                }
                payload.setDataset(payload.getFilename().substring(fromIdx, toIdx));
            }
        }
        return payload;
    }
    
    private void verifyArg(String param, String val) {
        if (longOptions.contains(val) || shortOptions.contains(val)) {
            throw new IllegalArgumentException("Missing value for option [" + param + "]");
        }
    }
}
