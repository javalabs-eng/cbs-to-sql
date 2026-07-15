package org.javalabs.tools.cbs2sql.cli.rel;

import java.util.Map;

/**
 * Handles columns that are encountered for the first time during schema introspection.
 *
 * <p>
 * When a previously unseen column is discovered in a subsequent JSON document, this rule determines whether it should
 * be added to the existing table definition and updates the schema accordingly while preserving column ordering and
 * nullability.</p>
 *
 * @author schan280
 */
public class NonExistingColumnRule {

    /**
     * Applies the reconciliation rule for a column that does not exist in the current consolidated table definition.
     *
     * <p>
     * This method determines whether a newly discovered column should be added to the existing table schema. Newly
     * added columns are marked as nullable because they were not present in previously analyzed documents. Special
     * handling is applied for empty array columns to avoid creating duplicate columns when the array has already been
     * promoted to a separate child table.</p>
     *
     * <p>
     * If the column is added, its ordinal position is updated so that it appears after the existing columns in the
     * consolidated schema.</p>
     *
     * @param tableMapping the mapping of table names to their consolidated analysis results
     * @param result the consolidated result for the table currently being updated
     * @param table the table definition inferred from the current JSON document
     * @param another the newly discovered column definition
     * @param current the corresponding column in the consolidated schema; reserved for rule compatibility and not used
     * by this implementation
     * @throws IllegalStateException if the consolidated schema cannot be updated consistently
     */
    public void apply(Map<String, Result> tableMapping, Result result, Table table, Column another, Column current) throws IllegalStateException {
        // Check if this is an ARRAY type.
        // E.g., 
        // Json 1: { "id":123, "addr":[{"pin":700032, "city":"kolkata"}] } => A table name addr is already created.
        // Json 2: { "id":678, "addr":[] }
        if (another.getType().equals("TEXT[]")) {
            // Check if the column "addr" is already treated as separate table.
            if (!tableMapping.containsKey(another.getName())) {
                //  Add this as a separate column only if the table does not exist
                another.setNullable(Boolean.TRUE);

                // Adjust the column order.
                another.setOrder(result.getTable().getColumns().size());
                result.getTable().addColumn(another);
            }
        } else {
            // Mark it as NULLABLE
            another.setNullable(Boolean.TRUE);

            // Adjust the column order.
            another.setOrder(result.getTable().getColumns().size());
            result.getTable().addColumn(another);
        }
    }
}
