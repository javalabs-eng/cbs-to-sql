package org.javalabs.tools.cbs2sql.cli.rel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.model.Transform;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;

/**
 * Generates SQL DDL and DML scripts from relational schema analysis results.
 *
 * <p>This class converts the relational model produced by the
 * {@link Introspector} into executable SQL scripts. Depending on the
 * configured transformation options, the generated scripts may be returned to
 * the caller or written to individual files on disk.</p>
 *
 * <p>The helper delegates SQL generation to {@link SchemaGen} and manages the
 * organization and persistence of the generated script files.</p>
 *
 * @author schan280
 */
public class ScriptHelper {

    /**
     * Generates SQL scripts from the supplied relational analysis results.
     *
     * <p>
     * For each analyzed table, this method generates the corresponding {@code CREATE TABLE} (DDL) and
     * {@code INSERT INTO} (DML) statements. The generated scripts are either returned as a string or written to
     * individual SQL files, depending on the transformation options.</p>
     *
     * <p>
     * When script dumping is enabled ({@code dump = "Y"}), separate {@code <table_name>_ddl.sql} and
     * {@code <table_name>_dml.sql} files are created under the configured output directory, and a summary message is
     * returned. Otherwise, the generated DDL script is returned directly.</p>
     *
     * @param payload the transformation options that control script generation, including the output directory and
     * whether scripts should be written to disk
     * @param results the relational analysis results containing the inferred table definitions and corresponding row
     * data
     * @return the generated DDL script when script dumping is disabled; otherwise, a summary describing the generated
     * SQL files
     * @throws RuntimeException if an error occurs while writing the generated scripts to disk
     */
    public String generateScript(Transform payload, Collection<Result> results) {
        StringBuilder ddlScript = new StringBuilder(81920);
        StringBuilder dmlScript = new StringBuilder(81920000);

        String dirName = prepareDirectory(payload);
        
        for (Result result : results) {
            // Prepare the ddl script.
            Table table = result.getTable();
            ddlScript.append(SchemaGen.generateTableScript(table, Boolean.FALSE));
            ddlScript.append("\n\n");

            // Prepare the dml script.
            List<Map<String, Object>> data = result.getData();

            // Ordered iteration of columns
            List<Object[]> rows = new ArrayList<>(data.size());

            for (Map<String, Object> entry : data) {
                Object[] row = new Object[table.getColumns().size()];
                rows.add(row);

                for (Column col : table.getColumns()) {
                    row[col.getOrder()] = entry.get(col.getName());
                }
            }
            dmlScript.append(SchemaGen.generateInsertScript(table, rows));
            
            if ("Y".equalsIgnoreCase(payload.getDump())) {
                dump(table, ddlScript, dirName, "_ddl");
                dump(table, dmlScript, dirName, "_dml");
            }
            ddlScript.delete(0, ddlScript.length());
            dmlScript.delete(0, dmlScript.length());
        }
        if ("Y".equalsIgnoreCase(payload.getDump())) {
            return new StringBuilder("Generated " + results.size() + " <table_name>_ddl.sql and <table_name>_dml.sql file(s) under " + dirName).toString();
        }
        else {
            return ddlScript.toString();
        }
    }

    private void dump(Table table, StringBuilder script, String dirName, String postfix) {
        // Dump the ddl and dml script to the directory.
        String filename = dirName + File.separator + table + postfix + ".sql";

        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        byte[] buff = new byte[8192];

        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw"); FileChannel channel = raf.getChannel()) {

            channel.truncate(0);        // optional: overwrite existing file
            channel.position(0);

            final int charChunkSize = 8192;
            char[] charBuffer = new char[charChunkSize];

            for (int pos = 0; pos < script.length(); pos += charChunkSize) {
                int len = Math.min(charChunkSize, script.length() - pos);

                script.getChars(pos, pos + len, charBuffer, 0);

                CharBuffer cb = CharBuffer.wrap(charBuffer, 0, len);
                ByteBuffer bb = encoder.encode(cb);

                while (bb.hasRemaining()) {
                    channel.write(bb);
                }
            }
        }
        catch (IOException e) {
            ConsoleWriter.println(String.format("Error writing ddl file %s to disk. Msg: %s",
                    (dirName + File.separator + "table_ddl.sql"), e.getMessage()));

            throw new RuntimeException(e);
        }
    }

    private String prepareDirectory(Transform payload) {
        String dirName;
        
        if (payload.getOutDir() != null) {
            dirName = payload.getOutDir();
        }
        else {
            if (payload.getDirectory() != null) {
                dirName = payload.getDirectory();
            }
            else {
                int idx = payload.getFilename().lastIndexOf("/");
                if (idx > 0) {
                    dirName = payload.getFilename().substring(0, idx);
                }
                else {
                    dirName = System.getProperty("user.dir");
                }
            }
        }
        dirName = dirName + File.separator + "script";
        File dir = new File(dirName);
        if (! dir.exists()) {
            dir.mkdir();
        }
        return dir.getAbsolutePath();
    }

}
