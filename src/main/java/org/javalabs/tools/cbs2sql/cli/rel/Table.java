package org.javalabs.tools.cbs2sql.cli.rel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an inferred relational database table.
 *
 * <p>A {@code Table} contains the column definitions, detected relationships
 * to other tables, and the row data extracted from JSON documents. It forms
 * the core data structure used throughout schema analysis and SQL script
 * generation.</p>
 *
 * @author schan280
 */
public class Table {
    
    private final String name;
    private List<Column> columns = new ArrayList<>();
    private List<Table> refTables = new ArrayList<>();
    
    private List<Object> data = new ArrayList<>();

    public Table(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
    
    public void addColumn(Column column) {
        columns.add(column);
    }

    public List<Table> getRefTables() {
        return refTables;
    }

    public void addRefTable(Table refTable) {
        refTables.add(refTable);
    }
    
    public void addData(Object val) {
        data.add(val);
    }

    public List<Object> getData() {
        return data;
    }
    
    public void clear() {
        data.clear();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Table other = (Table) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
