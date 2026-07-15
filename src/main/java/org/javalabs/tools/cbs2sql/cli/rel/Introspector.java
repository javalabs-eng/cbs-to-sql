package org.javalabs.tools.cbs2sql.cli.rel;

import org.javalabs.tools.cbs2sql.cli.model.Transform;
import org.javalabs.tools.cbs2sql.cli.util.MapperUtil;
import org.javalabs.tools.cbs2sql.cli.util.StopWatch;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.util.ConsoleWriter;
import org.javalabs.tools.cbs2sql.cli.util.GeneralUtility;

/**
 *
 * @author schan280
 */
public class Introspector {

    private final JsonDocAnalyzer analyzer;
    private final ScriptHelper scHelper;
    
    private final MatchingColumnRule mcRule = new MatchingColumnRule();
    private final NonExistingColumnRule neRule = new NonExistingColumnRule();

    public Introspector() {
        this.analyzer = new JsonDocAnalyzer();
        this.scHelper = new ScriptHelper();
    }
    
    public String introspectAndGenerateScript(Transform payload) {
        Collection<Result> results = introspect(payload);
        return scHelper.generateScript(payload, results);
    }

    public Collection<Result> introspect(Transform payload) {
        Collection<Result> results = null;
        
        if (payload.getFilename() != null) {
            results = scanDirectory(payload);
        }
        else {
            // For a single json document.
            results = scanDocument(payload);
        }

        // Check for duplicate primary key (id)
        List<Column> pkCols = new ArrayList<>();

        for (Result result : results) {
            Table table = result.getTable();
            
            List<Column> columns = table.getColumns();
            for (Column col : columns) {
                if (col.getPrimaryKey()) {
                    pkCols.add(col);
                }
            }
            // Now check the number of pks
            if (pkCols.size() > 1) {
                Boolean found = Boolean.FALSE;

                for (Iterator<Column> itr = pkCols.iterator(); itr.hasNext();) {
                    Column pkCol = itr.next();
                    if (pkCol.getName().equalsIgnoreCase("id") || pkCol.getName().equalsIgnoreCase("identifier")
                            || pkCol.getName().equalsIgnoreCase(table.getName() + "_id")
                            || pkCol.getName().equalsIgnoreCase(table.getName() + "_identifier")) {
                        // Found real pk. Discard others.
                        found = Boolean.TRUE;
                        itr.remove();
                        break;
                    }
                }
                if (found) {
                    for (Column pkCol : pkCols) {
                        pkCol.setPrimaryKey(Boolean.FALSE);
                    }
                }
                else {
                    for (Iterator<Column> itr = columns.iterator(); itr.hasNext();) {
                        Column pkCol = itr.next();
                        if (pkCol.getName().contains(table.getName())) {
                            // Found real pk. Discard others.
                            itr.remove();
                            break;
                        }
                    }
                    for (Column pkCol : pkCols) {
                        pkCol.setPrimaryKey(Boolean.FALSE);
                    }
                }
            }
            pkCols.clear();
        }

        // Arrange the columns.
        // Rule 1: Id (primary key) column should appear first.
        // Rule 2: Timestamp/Date column should appear at the end.
        for (Result result : results) {
            Table table = result.getTable();
            List<Column> columns = table.getColumns();
            List<Column> ordered = new ArrayList<>(columns.size());

            // Add primary key column first.
            int pkIdx = -1;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getPrimaryKey()) {
                    Column pkCol = columns.remove(i);
                    ordered.add(pkCol);
                    pkIdx = 0;
                    break;
                }
            }
            // Check Timestamp/Date column.
            int length = columns.size();
            for (int i = 0; i < length; i++) {
                if (columns.get(i).getType().equals("TIMESTAMP") || columns.get(i).getType().equals("DATE")) {
                    Column dtCol = columns.remove(i);
                    ordered.add(dtCol);
                    length -= 1;
                    i--;
                }
            }
            int dtIdx = pkIdx == -1 ? 0 : 1;
            if (! columns.isEmpty()) {
                ordered.addAll(dtIdx, columns);
            }
            // Check if this table has a reference table. If so, add the possible foreign key constraint.
            // Temporarily disabling foreign key constraint
            
            /*
            if (! table.getRefTables().isEmpty()) {
                // Get the primary key of that table.
                for (Table ref : table.getRefTables()) {
                    for (int i = 0; i < ref.getColumns().size(); i++) {
                        Column c = ref.getColumns().get(i);
                        if (c.getPrimaryKey()) {
                            Column col = c.cloneMe();
                            col.setPrimaryKey(Boolean.FALSE);
                            col.setForeignKey(Boolean.TRUE);
                            col.setReference(ref.getName());
                            col.setRefCol(c.getName());
                            ordered.add(col);

                            break;
                        }
                    }
                }
            }
            */
            
            // Now order the column order.
            for (int i = 0; i < ordered.size(); i ++) {
                Column col = ordered.get(i);
                col.setOrder(i);
            }
            table.setColumns(ordered);
        }
        return results;
    }

    public Collection<Result> scanDocument(Transform payload) {
        Collection<Result> results = new ArrayList<>();
        
        Collection<Table> tables = analyzer.analyze(payload);
        for (Table table : tables) {
            results.add(new Result(table));
        }
        return results;
    }

    public Collection<Result> scanDirectory(Transform payload) {
        StopWatch timer = StopWatch.newTimer();
        Map<String, Result> tableMapping = new HashMap<>();

        BufferedReader br = null;
        int lineNo = 0;
        File[] files = GeneralUtility.listEligibleDocs(payload);

        for (File file : files) {
            timer.start();
            String ds = file.getName().substring(0, file.getName().indexOf("."));
            payload.setDataset(ds);

            try {
                lineNo = 1;
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                
                for (String line = br.readLine(); line != null; line = br.readLine(), lineNo ++) {
                    LinkedHashMap<String, Object> json = MapperUtil.decode(line.getBytes(), LinkedHashMap.class);
                    payload.setDocument(json.containsKey("document") ? (LinkedHashMap)json.get("document") : json);

                    Collection<Table> tables = analyzer.analyze(payload);
                    
                    for (Table table : tables) {
                        Result result = tableMapping.get(table.getName());
                        if (result == null) {
                            tableMapping.put(table.getName(), (result = new Result(table)));
                        }
                        else {
                            // Analyze the column and see if any new column got added, or change the column size ...
                            Boolean found = Boolean.FALSE;
                            
                            for (Column another : table.getColumns()) {
                                for (Column current : result.getTable().getColumns()) {
                                    if (another.getName().equals(current.getName())) {
                                        mcRule.apply(table, another, current);
                                        
                                        found = Boolean.TRUE;
                                        break;
                                    }
                                }
                                if (! found) {
                                    neRule.apply(tableMapping, result, table, another, another);
                                }
                                found = Boolean.FALSE;
                            }
                            // Mark existing columns as NULLABLE if they are not found in new set.
                            for (Column current : result.getTable().getColumns()) {
                                for (Column another : table.getColumns()) {
                                    if (current.getName().equals(another.getName())) {
                                        found = Boolean.TRUE;
                                        break;
                                    }
                                }
                                if (! found) {
                                    current.setNullable(Boolean.TRUE);
                                }
                                found = Boolean.FALSE;
                            }
                        }
                        result.addData(table);
                    }
                }
            }
            catch (Exception e) {
                ConsoleWriter.println(String.format("Analysis failed for dataset %s and file %s. Error: %s",
                         ds, file.getAbsolutePath(), e.getMessage()));
                throw new RuntimeException(e);
            }
            finally {
                if (br != null) {
                    try {
                        br.close();
                    }
                    catch (IOException ex) {
                        // Do Nothing
                    }
                }
            }
            timer.stop();
            if ("Y".equalsIgnoreCase(payload.getVerbose())) {
                ConsoleWriter.println(String.format(
                        "Analyzed dataset %s and file %s. Generated %d table(s). Elapsed time(ms): %d",
                         ds,
                         file.getAbsolutePath(),
                         tableMapping.size(),
                         timer.elapsedTimeMillis()));
            }
        }
        // Json 1: { "id":678, "addr":[] }
        // Json 2: { "id":123, "addr":[{"pin":700032, "city":"kolkata"}] }
        for (Result result : tableMapping.values()) {
            Table table = result.getTable();
            Boolean adjustOrder = Boolean.FALSE;
            
            for (Iterator<Column> itr = table.getColumns().iterator(); itr.hasNext(); ) {
                Column col = itr.next();
                if (col.getType().equals("TEXT[]") && tableMapping.containsKey(col.getName())) {
                    // We need to remove this array column, as it is turned out to be a separate table.
                    itr.remove();
                    adjustOrder = Boolean.TRUE;
                }
            }
            if (adjustOrder) {
                for (int k = 0; k < table.getColumns().size(); k ++) {
                    table.getColumns().get(k).setOrder(k);
                }
            }
        }
        return tableMapping.values();
    }
}
