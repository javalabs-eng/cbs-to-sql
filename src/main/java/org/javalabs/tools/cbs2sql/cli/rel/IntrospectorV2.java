package org.javalabs.tools.cbs2sql.cli.rel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author schan280
 */
public class IntrospectorV2 {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String json = """
        {
          "client_institution_relationship":[
            {"client_institution_relationship_create_timestamp":"2020-05-26 23:23:34.852"}
          ],
          "client_onboarding_status_text":"COMPLETED",
          "domicile_country":{"client_domicile_country_name":"COSTA RICA"},
          "institution_blocked_bin":[],
          "primary_institution_indicator":true
        }
        """;

        SchemaResult result = generateSchema(json, "root_document");
        System.out.println(result.toSql());
    }

    public static SchemaResult generateSchema(String json, String rootTableName) throws Exception {
        JsonNode root = MAPPER.readTree(json);

        SchemaContext ctx = new SchemaContext();
        TableDef rootTable = new TableDef(sanitize(rootTableName), null);
        rootTable.addColumnIfAbsent(new ColumnDef("id", "BIGSERIAL", false, true, null));

        if (root.isObject()) {
            processObject(root, rootTable, ctx);
        } else if (root.isArray()) {
            // Wrap top-level array into a root table with child rows
            processArray(root, rootTable, ctx, "item");
        } else {
            rootTable.addColumnIfAbsent(new ColumnDef("value", inferSqlType(root), true, false, null));
        }

        ctx.tables.put(rootTable.name, rootTable);
        return new SchemaResult(ctx.tables.values());
    }

    private static void processObject(JsonNode obj, TableDef table, SchemaContext ctx) {
        Iterator<Map.Entry<String, JsonNode>> fields = obj.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> f = fields.next();
            String fieldName = sanitize(f.getKey());
            JsonNode value = f.getValue();

            if (value == null || value.isNull()) {
                table.addColumnIfAbsent(new ColumnDef(fieldName, "TEXT", true, false, null));
                continue;
            }

            if (value.isValueNode()) {
                table.addColumnIfAbsent(new ColumnDef(fieldName, inferSqlType(value), true, false, null));
            } else if (value.isObject()) {
                String childTableName = table.name + "_" + fieldName;
                TableDef child = ctx.getOrCreate(childTableName, table.name);
                child.addColumnIfAbsent(new ColumnDef("id", "BIGSERIAL", false, true, null));
                child.addColumnIfAbsent(new ColumnDef(table.name + "_id", "BIGINT", false, false, table.name));
                processObject(value, child, ctx);
            } else if (value.isArray()) {
                processArray(value, table, ctx, fieldName);
            } else {
                table.addColumnIfAbsent(new ColumnDef(fieldName, "JSONB", true, false, null));
            }
        }
    }

    private static void processArray(JsonNode array, TableDef parentTable, SchemaContext ctx, String fieldName) {
        String childTableName = parentTable.name + "_" + sanitize(fieldName);
        TableDef child = ctx.getOrCreate(childTableName, parentTable.name);
        child.addColumnIfAbsent(new ColumnDef("id", "BIGSERIAL", false, true, null));
        child.addColumnIfAbsent(new ColumnDef(parentTable.name + "_id", "BIGINT", false, false, parentTable.name));
        child.addColumnIfAbsent(new ColumnDef("item_index", "INTEGER", false, false, null));

        if (array.isEmpty()) {
            // Unknown content; use jsonb for generic safety
            child.addColumnIfAbsent(new ColumnDef("value", "JSONB", true, false, null));
            return;
        }

        boolean allPrimitives = true;
        boolean allObjects = true;

        for (JsonNode item : array) {
            if (item == null || item.isNull()) {
                continue;
            }
            if (item.isValueNode()) {
                allObjects = false;
            } else if (item.isObject()) {
                allPrimitives = false;
            } else {
                allPrimitives = false;
                allObjects = false;
            }
        }

        if (allPrimitives) {
            // Primitive array -> value table
            String primitiveType = "TEXT";
            for (JsonNode item : array) {
                if (item != null && !item.isNull()) {
                    primitiveType = inferSqlType(item);
                    break;
                }
            }
            child.addColumnIfAbsent(new ColumnDef("value", primitiveType, true, false, null));
        } else if (allObjects) {
            // Array of objects -> child table with union of fields
            for (JsonNode item : array) {
                if (item != null && item.isObject()) {
                    processObject(item, child, ctx);
                }
            }
        } else {
            // Mixed array -> safest is JSONB
            child.addColumnIfAbsent(new ColumnDef("value", "JSONB", true, false, null));
        }
    }

    private static String inferSqlType(JsonNode node) {
        if (node == null || node.isNull()) {
            return "TEXT";
        }
        if (node.isBoolean()) {
            return "BOOLEAN";
        }
        if (node.isInt() || node.isLong() || node.isShort() || node.isBigInteger()) {
            return "BIGINT";
        }
        if (node.isFloat() || node.isDouble() || node.isBigDecimal()) {
            return "NUMERIC";
        }
        if (node.isTextual()) {
            String s = node.asText();

            if (looksLikeTimestamp(s)) {
                return "TIMESTAMP";
            }
            if (looksLikeDate(s)) {
                return "DATE";
            }

            return "TEXT";
        }
        return "JSONB";
    }

    private static boolean looksLikeDate(String s) {
        return s != null && s.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    private static boolean looksLikeTimestamp(String s) {
        return s != null && (s.matches("^\\d{4}-\\d{2}-\\d{2}T.*$")
                || s.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*$"));
    }

    private static String sanitize(String name) {
        if (name == null || name.isBlank()) {
            return "unnamed";
        }
        String s = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        s = s.replaceAll("^_+|_+$", "");
        if (s.isBlank()) {
            return "unnamed";
        }
        if (Character.isDigit(s.charAt(0))) {
            s = "c_" + s;
        }
        return s;
    }

    public static final class SchemaResult {

        private final Collection<TableDef> tables;

        public SchemaResult(Collection<TableDef> tables) {
            this.tables = tables;
        }

        public String toSql() {
            StringBuilder sb = new StringBuilder();
            List<TableDef> ordered = new ArrayList<>(tables);
            ordered.sort(Comparator.comparing(t -> t.name));

            for (TableDef table : ordered) {
                sb.append(table.toCreateTableSql()).append("\n\n");
            }
            return sb.toString();
        }
    }

    public static final class SchemaContext {

        private final Map<String, TableDef> tables = new LinkedHashMap<>();

        public TableDef getOrCreate(String tableName, String parentTableName) {
            return tables.computeIfAbsent(tableName, n -> {
                TableDef t = new TableDef(sanitize(n), parentTableName);
                return t;
            });
        }
    }

    public static final class TableDef {

        private final String name;
        private final String parentTableName;
        private final LinkedHashMap<String, ColumnDef> columns = new LinkedHashMap<>();
        private boolean foreignKeyAdded = false;

        public TableDef(String name, String parentTableName) {
            this.name = name;
            this.parentTableName = parentTableName;
        }

        public void addColumnIfAbsent(ColumnDef col) {
            columns.putIfAbsent(col.name, col);
        }

        public String toCreateTableSql() {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE ").append(name).append(" (\n");

            List<String> lines = new ArrayList<>();
            for (ColumnDef c : columns.values()) {
                StringBuilder line = new StringBuilder();
                line.append("    ").append(c.name).append(" ").append(c.sqlType);
                if (!c.nullable) {
                    line.append(" NOT NULL");
                }
                if (c.primaryKey) {
                    line.append(" PRIMARY KEY");
                }
                lines.add(line.toString());
            }

            if (parentTableName != null && !columns.containsKey(parentTableName + "_id")) {
                lines.add("    " + parentTableName + "_id BIGINT NOT NULL");
            }

            if (parentTableName != null) {
                lines.add("    FOREIGN KEY (" + parentTableName + "_id) REFERENCES " + parentTableName + "(id)");
            }

            sb.append(String.join(",\n", lines));
            sb.append("\n);");
            return sb.toString();
        }
    }

    public static final class ColumnDef {

        private final String name;
        private final String sqlType;
        private final boolean nullable;
        private final boolean primaryKey;
        private final String referenceTable;

        public ColumnDef(String name, String sqlType, boolean nullable, boolean primaryKey, String referenceTable) {
            this.name = name;
            this.sqlType = sqlType;
            this.nullable = nullable;
            this.primaryKey = primaryKey;
            this.referenceTable = referenceTable;
        }
    }

}
