package org.javalabs.tools.cbs2sql.cli.sync;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.model.SyncOptions;
import org.javalabs.tools.cbs2sql.cli.storage.Storage;
import org.javalabs.tools.cbs2sql.cli.storage.impl.CBQueryConfig;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;
import org.javalabs.tools.cbs2sql.cli.util.StopWatch;

/**
 *
 * @author schan280
 */
public class CbExporter {

    private static final String SELECT_QUERY
            = "  SELECT META().id AS id, {0} AS document"
            + "  FROM {1}"
            + " ORDER BY META().id"
            + " OFFSET $offset LIMIT $limit";

    private static final String DS_SELECT_QUERY
            = "  SELECT META().id AS id, {0} AS document"
            + "  FROM {1}"
            + " WHERE {2}"
            + " ORDER BY META().id"
            + " OFFSET $offset LIMIT $limit";

    private static final ByteBuffer NEW_LINE = ByteBuffer.wrap("\n".getBytes());

    /**
     * Exports documents from the specified bucket, scope, and collection to one or more JSON files.
     *
     * <p>
     * Documents are read from the source storage in batches and written to the specified output directory. The export
     * can be limited to a maximum number of documents and may optionally produce verbose output.
     *
     * @param source The source {@link Storage} instance from which data is exported.
     * @param dataset The bucket name containing the data to export.
     * @param table The scope and collection identifier of the source data.
     * @param total The maximum number of documents to export; {@code null} exports all matching documents.
     * @param opts The export options.
     *
     * @throws IOException if an error occurs while creating or writing the export files
     */
    public void _export(Storage source,
             String dataset,
             String table,
             Integer total,
             SyncOptions opts) throws IOException {

        StopWatch timer = StopWatch.newTimer();

        Integer iteration = total % opts.getBatchSize() == 0 ? total / opts.getBatchSize() : total / opts.getBatchSize() + 1;
        if ("Y".equalsIgnoreCase(opts.getVerbose())) {
            ConsoleWriter.println(String.format(
                    "Started to export dataset '%s' from %s. Total: %d. Iteration count: %d",
                    (dataset != null ? dataset : "ALL"),
                    source.host(),
                    total,
                    iteration));
        }

        // Split the table (`bucket`.`scope`.`collection`) into bucket, scope and collection
        String[] tmp = table.split("`\\.`");
        tmp[0] = tmp[0].replace("`", "");
        tmp[2] = tmp[2].replace("`", "");

        File outDir = new File(opts.getOutDir());
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        RandomAccessFile writer = new RandomAccessFile(outDir.getAbsolutePath()
                + File.separator
                + dataset + opts.getExtn(), "rw");

        final FileChannel channel = writer.getChannel();

        CBQueryConfig fQuery = new CBQueryConfig();
        fQuery.setBatch(Boolean.TRUE);
        fQuery.setRaw(Boolean.TRUE);

        try {
            String query = MessageFormat.format(SELECT_QUERY, tmp[2], table);
            if (dataset != null && opts.getIdxCol() != null) {
                String where = opts.getIdxCol() + " = $" + opts.getIdxCol();
                query = MessageFormat.format(DS_SELECT_QUERY, tmp[2], table, where);
            }

            ByteBuffer buff = ByteBuffer.allocate(16 * 1024);       // Assuming max length of a document will not exceed 16 KB.

            for (int offset = 0, idx = 0; idx < iteration; idx++, offset += opts.getBatchSize()) {
                timer.start();

                fQuery.setN1ql(query);
                if (dataset != null && opts.getIdxCol() != null) {
                    fQuery.setParameters(Map.of(opts.getIdxCol(), dataset, "offset", offset, "limit", opts.getBatchSize()));
                }
                else {
                    fQuery.setParameters(Map.of("offset", offset, "limit", opts.getBatchSize()));
                }
                fQuery.setBatch(Boolean.TRUE);

                List<byte[]> rows = source.export(fQuery);
                timer.stop();
                Long qTime = timer.elapsedTimeMillis();

                timer.reset();
                timer.start();

                // Write the content to the underlying file.
                writeToFile(channel, rows, buff);

                timer.stop();

                if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                    ConsoleWriter.println(String.format(
                            "Exported %d record(s). Offset: %d, Limit: %d, Query time(ms): %d, Write time(ms): %d",
                                (offset + (rows.size() < opts.getBatchSize() ? rows.size() : opts.getBatchSize())),
                                offset,
                                opts.getBatchSize(),
                                rows.size(),
                                qTime,
                                timer.elapsedTimeMillis()));
                }
                timer.reset();
            }
            if ("Y".equalsIgnoreCase(opts.getVerbose())) {
                ConsoleWriter.println(String.format("File is generated at: %s. Size (byte): %,d"
                        , outDir.getAbsolutePath() + File.separator + dataset + opts.getExtn()
                        , channel.size()));
            }
        }
        finally {
            channel.close();
            writer.close();
        }
    }

    private void writeToFile(FileChannel channel, List<byte[]> rows, ByteBuffer buff) throws IOException {
        for (byte[] row : rows) {
            try {
                buff.put(row);
            } catch (BufferOverflowException e) {
                buff.clear();
                buff = ByteBuffer.allocate(row.length);

                buff.put(row);
            }
            buff.flip();

            channel.write(buff);
            channel.write(NEW_LINE);
            NEW_LINE.flip();

            buff.clear();
        }
    }
}
