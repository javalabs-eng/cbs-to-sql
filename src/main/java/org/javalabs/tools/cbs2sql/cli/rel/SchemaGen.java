package org.javalabs.tools.cbs2sql.cli.rel;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating SQL schema and data manipulation scripts.
 *
 * <p>
 * This class provides helper methods for mapping Java data types to SQL data types and for generating
 * {@code CREATE TABLE} and {@code INSERT INTO} statements from the inferred relational model.</p>
 *
 * <p>
 * The generated SQL is intended to recreate the relational structure and populate it with data extracted from JSON
 * documents.</p>
 *
 * @author schan280
 */
public final class SchemaGen {

    private static final Map<Class<?>, String> DATATYPE_MAPPING = new HashMap<>();

    static {
        DATATYPE_MAPPING.put(Array.class, "TEXT[]");
        DATATYPE_MAPPING.put(byte[].class, "BYTEA");
        DATATYPE_MAPPING.put(String.class, "VARCHAR");
        DATATYPE_MAPPING.put(Boolean.class, "SMALLINT");
        DATATYPE_MAPPING.put(Byte.class, "SMALLINT");
        DATATYPE_MAPPING.put(Short.class, "INT");
        DATATYPE_MAPPING.put(Integer.class, "INT");
        DATATYPE_MAPPING.put(Long.class, "BIGINT");
        DATATYPE_MAPPING.put(Float.class, "NUMERIC(10,2)");
        DATATYPE_MAPPING.put(Double.class, "NUMERIC(10,2)");
        DATATYPE_MAPPING.put(BigDecimal.class, "NUMERIC(20,6)");
        DATATYPE_MAPPING.put(Timestamp.class, "TIMESTAMP");
        DATATYPE_MAPPING.put(Date.class, "DATE");
        DATATYPE_MAPPING.put(Time.class, "TIME");
    }

    public static String sqlDataType(Class<?> javatype) {
        String dbDataType = DATATYPE_MAPPING.get(javatype);
        if (dbDataType == null) {
            throw new IllegalArgumentException("No postgres data type found for java type: " + javatype.getSimpleName());
        }
        return dbDataType;
    }

    public static String sqlArrayDataType(Class<?> javatype) {
        String dbDataType = DATATYPE_MAPPING.get(javatype);
        if (dbDataType == null) {
            throw new IllegalArgumentException("No postgres data type found for java type: " + javatype.getSimpleName());
        }
        if (!dbDataType.equals("TEXT[]")) {
            dbDataType += "[]";
        }
        return dbDataType;
    }

    /**
     * Generates a SQL {@code CREATE TABLE} statement for the supplied relational table definition.
     *
     * <p>
     * The generated script includes the table name, column definitions, data types, length specifications (where
     * applicable), nullability constraints, primary key declarations, and, optionally, foreign key constraints. Column
     * definitions are formatted for readability by aligning names and data types.</p>
     *
     * @param table the relational table definition from which the SQL DDL is to be generated
     * @param printFk {@code true} to include foreign key constraints in the generated script; {@code false} to omit
     * foreign key columns and constraints
     * @return a SQL {@code CREATE TABLE} statement representing the supplied table definition
     * @throws IllegalArgumentException if the table definition contains invalid or unsupported column metadata
     */
    public static String generateTableScript(Table table, Boolean printFk) {
        List<Column> columns = table.getColumns();
        int colLength = 0;
        int dtypeLength = 0;

        StringBuilder script = new StringBuilder(2048);
        script.append("--              Script for table: ").append(table.getName()).append("              --");
        script.append("\n\n");
        script.append("CREATE TABLE ").append(table.getName()).append(" (");

        int cDiff = 0;
        int dDiff = 0;

        for (int i = 0; i < columns.size(); i++) {
            String name = columns.get(i).getName();
            if (name.length() > colLength) {
                colLength = name.length();
            }
            String dtype = columns.get(i).getType();
            if (dtype.length() > dtypeLength) {
                dtypeLength = dtype.length();
                if ("VARCHAR".equals(columns.get(i).getType())) {
                    dtypeLength += 2 + String.valueOf(columns.get(i).getLength()).length();
                }
                if (dtypeLength > 18) {
                    dtypeLength = 18;
                }
            }
        }

        colLength += 2;
        dtypeLength += 2;

        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            if (col.getForeignKey() && !printFk) {
                continue;
            }

            // Add column name
            script.append("\n    ").append(col.getName());

            // Formatting 1 ...
            cDiff = colLength - col.getName().length();
            for (int j = 0; j < cDiff; j++) {
                script.append(" ");
            }

            // Add data type
            script.append(col.getType());
            if ("VARCHAR".equals(col.getType())) {
                script.append("(").append(col.getLength()).append(")");
            }

            // Formatting 2 ...
            dDiff = dtypeLength - col.getType().length();
            if ("VARCHAR".equals(col.getType())) {
                dDiff = dDiff - (2 + String.valueOf(col.getLength()).length());
            }
            for (int j = 0; j < dDiff; j++) {
                script.append(" ");
            }

            // Add null or not null clause
            script.append(col.getNullable() ? "" : "NOT NULL");
            script.append(col.getPrimaryKey() ? " PRIMARY KEY" : "");
            script.append(col.getForeignKey() ? " REFERENCES " + col.getReference() + " (" + col.getRefCol() + ")" : "");

            if (i < columns.size() - 1) {
                script.append(",");
            }
        }
        script.append("\n)").append(";");

        return script.toString();
    }

    /**
     * Generates SQL {@code INSERT INTO} statements for the supplied table and row data.
     *
     * <p>
     * The generated script contains a single multi-row {@code INSERT} statement for the specified table. Values are
     * formatted according to their Java types, with strings being quoted and escaped, booleans converted to numeric
     * values, arrays represented using SQL array syntax, and {@code null} values emitted as SQL {@code NULL}
     * literals.</p>
     *
     * <p>
     * The column order in the generated statement is determined by the associated {@link Table} definition, and each
     * element of the supplied row arrays is expected to correspond to the column at the same ordinal position.</p>
     *
     * @param table the relational table definition containing the target table name and column metadata
     * @param rows the row data to be included in the generated {@code INSERT INTO} statement, where each array
     * represents a single table row
     * @return a SQL script containing a multi-row {@code INSERT INTO} statement for the supplied table and data
     * @throws IllegalArgumentException if the row data is inconsistent with the table definition
     */
    public static String generateInsertScript(Table table, List<Object[]> rows) {
        StringBuilder script = new StringBuilder(4096000);

        script.append("--              Insert Statements for table: ").append(table.getName()).append("              --");
        script.append("\n\n");
        script.append("INSERT INTO ").append(table.getName()).append("(");
        for (Column col : table.getColumns()) {
            script.append(col.getName()).append(", ");
        }
        script.delete(script.length() - 2, script.length());
        script.append(")").append(" VALUES");
        script.append("\n");

        for (int n = 0; n < rows.size(); n++) {
            Object[] row = rows.get(n);
            script.append("(");
            for (int i = 0; i < row.length; i++) {
                if (row[i] == null) {
                    script.append("null");
                } else if (row[i].getClass() == String.class) {
                    // Here timestamp is also included.
                    String val = (String) row[i];
                    if (val.indexOf("'") > 0) {
                        val = val.replace("'", "''");
                    }
                    script.append("'").append(val).append("'");
                } else if (row[i].getClass() == Boolean.class || row[i].getClass() == boolean.class) {
                    if (((Boolean) row[i])) {
                        script.append("1");
                    } else {
                        script.append(0);
                    }
                } else if (row[i].getClass().isArray() || Collection.class.isAssignableFrom(row[i].getClass())) {
                    String v = "ARRAY[";
                    for (Object o : (Collection) row[i]) {
                        if (o.getClass() == String.class) {
                            v = v + "'" + (String) o + "'" + ", ";
                        } else {
                            v = v + o + ", ";
                        }
                    }
                    v = v.substring(0, v.length() - 2);
                    v += "]";

                    script.append(v);
                } else {
                    script.append(row[i]);
                }
                script.append(", ");
            }
            script.delete(script.length() - 2, script.length());
            script.append(")");

            if (n < rows.size() - 1) {
                script.append(",");
            } else {
                script.append(";");
            }
            script.append("\n");
        }

        return script.toString();
    }
}
