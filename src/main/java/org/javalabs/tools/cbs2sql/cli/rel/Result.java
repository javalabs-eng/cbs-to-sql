package org.javalabs.tools.cbs2sql.cli.rel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the relational analysis result for a single table.
 *
 * <p>A {@code Result} combines the inferred table definition with the data
 * rows extracted from one or more JSON documents. It serves as the
 * intermediate representation between schema analysis and SQL script
 * generation.</p>
 *
 * @author schan280
 */
public class Result {
    
    private Table table;
    private List<Map<String, Object>> data = new ArrayList<>(100);
    
    public Result() {}

    public Result(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setRows(List<Map<String, Object>> data) {
        this.data = data;
    }
    
    public void addData(Table table) {
        // Add the row.
        Map<String, Object> values = new HashMap<>();
        int order = 0;
        for (Object val : table.getData()) {
            values.put(table.getColumns().get(order ++).getName(), val);
        }
        data.add(values);
    }
    
}
