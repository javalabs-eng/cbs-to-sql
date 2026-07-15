package org.javalabs.tools.cbs2sql.cli.rel;

import org.javalabs.tools.cbs2sql.cli.util.GeneralUtility;
import java.sql.Array;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.model.Transform;

/**
 * Json document analyzer.
 *
 * @author schan280
 */
public class JsonDocAnalyzer {

    /**
     * Analyzes a JSON document and generates a collection of relational table definitions representing the document
     * structure.
     *
     * @param payload   It contains:
     *                  1. The logical name of the dataset. This name is used as the root table name for the generated
     *                  schema.
     *                  2. The JSON document represented as a map. The map is expected to contain nested maps, lists, and
     *                  primitive values corresponding to the JSON structure.
     *                  Other attributes.
     *
     * @return a collection of {@link Table} objects representing the generated relational schema for the supplied JSON
     * document. The collection includes the root table and any child tables created during analysis.
     * 
     * @throws IllegalArgumentException if {@code dataset} or {@code json} is {@code null}, or if the JSON structure
     * cannot be analyzed.
     */
    public Collection<Table> analyze(Transform payload) {
        List<Table> tables = new ArrayList<>();
        parse(payload.getDataset(), payload.getDocument(), tables, "Y".equalsIgnoreCase(payload.getFlatten()));

        return tables;
    }

    /**
     * Parse a JSON document and generates a collection of relational table definitions representing the document
     * structure.
     *
     * <p>
     * The method recursively traverses the supplied JSON object and maps its structure to a relational model. Primitive
     * fields become table columns, nested objects may be flattened into the parent table or represented as separate
     * child tables depending on the value of {@code flattenChild}, and arrays are analyzed to determine the appropriate
     * relational representation.</p>
     *
     * @param dataset   The logical name of the dataset. This name is used as the root table name for the generated
     *                  schema.
     * @param json      The JSON document represented as a map. The map is expected to contain nested maps, lists, and
     *                  primitive values corresponding to the JSON structure.
     * @param tables    The set of tables to be generated.
     * @param flattenChild  If {@code true}, eligible nested child objects are flattened into the parent table;
     *                      otherwise, child objects are represented as separate relational tables linked by
     *                      foreign-key relationships.
     *
     * @throws IllegalArgumentException if {@code dataset} or {@code json} is {@code null}, or if the JSON structure
     * cannot be analyzed.
     */
    public void parse(String dataset, Map<String, Object> json, List<Table> tables, Boolean flattenChild) {
        Table table = new Table(dataset);
        tables.add(table);

        int idx = 0;
        for (Iterator<Map.Entry<String, Object>> itr = json.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry<String, Object> me = itr.next();
            String key = me.getKey();
            Object val = me.getValue();

            if (val == null || (val.getClass() == String.class && ((String)val).trim().length() == 0)) {
                // Consider the data type as string. Even empty string is also treated as null
                Column col = column(key, idx ++, String.class);
                col.setNullable(Boolean.TRUE);
                table.addColumn(col);
                table.addData(null);
                
                continue;
            }
            // If the value is of primitive type, straight away consider them as columns.
            if (GeneralUtility.isPrimitive(val.getClass())) {
                Column col = column(key, idx ++, val.getClass());
                if (val.getClass() == String.class) {
                    col.setLength(((String) val).length() * 2);
                }
                table.addColumn(col);
                table.addData(val.getClass() == String.class ? ((String)val).trim() : val);

                // overrideDate(key, col);                  // Override date for a simple attribute
                overrideDate(key, col, val);                // Override date for a simple attribute
                overridePk(table.getName(), key, col);      // Primary key check is only for simple attribute that has some value.
            }
            else if (Map.class.isAssignableFrom(val.getClass())) {
                Boolean nonPrimitiveChild = isExistNonPrimitiveType((Map) val);

                if (nonPrimitiveChild || !flattenChild) {
                    int currentTableIdx = tables.size() - 1;
                    parse(key, (Map) val, tables, flattenChild);

                    Table refTable = tables.get(currentTableIdx + 1);
                    table.addRefTable(refTable);
                }
                else {
                    // The sub map does not contain further non-primitive type.
                    // Therefore include them part of the parent table
                    Map child = (Map) val;

                    for (Iterator<Map.Entry<String, Object>> childItr = child.entrySet().iterator(); childItr.hasNext(); ) {
                        Map.Entry<String, Object> childMe = childItr.next();
                        String childKey = childMe.getKey();
                        Object childVal = childMe.getValue();

                        if (childVal == null || (childVal.getClass() == String.class && ((String)childVal).trim().length() == 0)) {
                            // Consider the data type as string.
                            Column col = column(childKey, idx ++, String.class);
                            col.setNullable(Boolean.TRUE);
                            table.addColumn(col);
                            table.addData(null);
                            
                            continue;
                        }
                        if (GeneralUtility.isPrimitive(childVal.getClass())) {
                            Column col = column(childKey, idx ++, childVal.getClass());
                            if (childVal.getClass() == String.class) {
                                col.setLength(((String) childVal).length() * 2);
                            }
                            table.addColumn(col);
                            table.addData(childVal.getClass() == String.class ? ((String)childVal).trim() : val);

                            overrideDate(childKey, col, childVal);              // Override date for a simple attribute
                            overridePk(table.getName(), childKey, col);
                        }
                        else if (Collection.class.isAssignableFrom(childVal.getClass()) && ((Collection) childVal).isEmpty()) {
                            Column col = arrayColumn(childKey, idx ++, Array.class);
                            col.setNullable(Boolean.TRUE);
                            table.addColumn(col);
                            table.addData(null);
                        }
                    }
                }
            }
            else if (val.getClass().isArray()) {
                table.addColumn(arrayColumn(key, idx ++, Array.class));
                table.addData(val);
            }
            else if (Collection.class.isAssignableFrom(val.getClass())) {
                if (((Collection) val).isEmpty()) {
                    Column col = arrayColumn(key, idx ++, Array.class);
                    col.setNullable(Boolean.TRUE);
                    table.addColumn(col);
                    table.addData(null);
                }
                else {
                    Collection coll = (Collection) val;
                    Iterator<?> collItr = coll.iterator();
                    collItr.hasNext();
                    Object element = collItr.next();

                    if (GeneralUtility.isPrimitive(element.getClass())) {
                        // We are purposely sending the the data type of the occupied element to get the right SQL array.
                        // E.g., VARCHAR[] or INT[], etc.
                        // However, post that, the final java type will be Array.class
                        Column col = arrayColumn(key, idx ++, element.getClass());
                        col.setJavaType(Array.class);
                        
                        table.addColumn(col);
                        table.addData(val);
                    }
                    else if (Map.class.isAssignableFrom(element.getClass())) {
                        // Take first element.
                        int currentTableIdx = tables.size() - 1;
                        parse(key, (Map) element, tables, flattenChild);

                        Table refTable = tables.get(currentTableIdx + 1);
                        table.addRefTable(refTable);
                    }
                    else {
                        throw new IllegalArgumentException("Invalid array type " + element.getClass() + " for attribute " + key);
                    }
                }
            }
            else {
                throw new IllegalArgumentException("Unsupported attribute type " + val.getClass() + " for attribute " + key);
            }
        }
    }

    private Column column(String key, Integer idx, Class<?> clazz) {
        Column col = new Column(key);
        col.setOrder(idx);
        col.setJavaType(clazz);
        col.setType(SchemaGen.sqlDataType(clazz));

        overrideDate(key, col);
        return col;
    }

    private Column arrayColumn(String key, Integer idx, Class<?> clazz) {
        Column col = new Column(key);
        col.setOrder(idx);
        col.setJavaType(clazz);
        col.setType(SchemaGen.sqlArrayDataType(clazz));
        return col;
    }

    private void overrideDate(String key, Column col) {
        overrideDate(key, col, null);
    }

    private void overrideDate(String key, Column col, Object val) {
        if (val != null && val.toString().matches(GeneralUtility.TS_REGEXP)) {
            col.setType(SchemaGen.sqlDataType(Timestamp.class));
        }
        else if (val != null && val.toString().matches(GeneralUtility.DT_REGEXP)) {
            col.setType(SchemaGen.sqlDataType(Date.class));
        }
        else if (key.endsWith("_timestamp") || key.endsWith("_time") || key.endsWith("_at")) {
            col.setType(SchemaGen.sqlDataType(Timestamp.class));
        }
        else if (key.endsWith("_date") || key.endsWith("_dt")) {
            col.setType(SchemaGen.sqlDataType(Date.class));
        }
    }

    private void overridePk(String table, String key, Column col) {
        if (key.equalsIgnoreCase("identifier") || key.equalsIgnoreCase("id")
                || key.equalsIgnoreCase(table + "_id")
                || key.equalsIgnoreCase(table + "_identifier")) {

            col.setPrimaryKey(Boolean.TRUE);
        }
        else {
            int idx = key.indexOf("_id");
            if (idx > 0) {
                String sub = key.substring(0, idx);
                if (table.contains(sub)) {
                    col.setPrimaryKey(Boolean.TRUE);
                }
            }
        }
    }

    /**
     * Check if this json has all primitive attributes.
     *
     * @param json
     */
    private Boolean isExistNonPrimitiveType(Map<String, Object> json) {
        for (Iterator<Map.Entry<String, Object>> itr = json.entrySet().iterator(); itr.hasNext();) {
            Map.Entry<String, Object> me = itr.next();
            String key = me.getKey();
            Object val = me.getValue();

            if (val == null || GeneralUtility.isPrimitive(val.getClass())
                    || (Collection.class.isAssignableFrom(val.getClass()) && ((Collection) val).isEmpty())) {

                // Primitive data type
            }
            else {
                // Non-primitive type
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
