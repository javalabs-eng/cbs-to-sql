package org.javalabs.tools.cbs2sql.cli.rel;

import java.util.Map;

/**
 *
 * @author schan280
 */
public class NonExistingColumnRule {

    public void apply(Map<String, Result> tableMapping, Result result, Table table, Column another, Column current) throws IllegalStateException {
        // Check if this is an ARRAY type.
        // E.g., 
        // Json 1: { "id":123, "addr":[{"pin":700032, "city":"kolkata"}] } => A table name addr is already created.
        // Json 2: { "id":678, "addr":[] }
        if (another.getType().equals("TEXT[]")) {
            // Check if the column "addr" is already treated as separate table.
            if (! tableMapping.containsKey(another.getName())) {
                //  Add this as a separate column only if the table does not exist
                another.setNullable(Boolean.TRUE);

                // Adjust the column order.
                another.setOrder(result.getTable().getColumns().size());
                result.getTable().addColumn(another);
            }
        }
        else {
            // Mark it as NULLABLE
            another.setNullable(Boolean.TRUE);

            // Adjust the column order.
            another.setOrder(result.getTable().getColumns().size());
            result.getTable().addColumn(another);
        }
    }
}
