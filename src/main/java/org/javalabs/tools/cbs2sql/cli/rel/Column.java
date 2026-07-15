package org.javalabs.tools.cbs2sql.cli.rel;

import java.util.Objects;

/**
 * Represents a column in an inferred relational table.
 *
 * <p>A {@code Column} encapsulates the metadata required to generate a SQL
 * column definition, including its name, SQL and Java data types, size,
 * ordering, nullability, key constraints, and optional foreign-key
 * relationship information.</p>
 *
 * @author schan280
 */
public class Column implements Cloneable {
    
    private final String name;
    private String type;
    private Class<?> javaType;
    private Integer length = 10;
    private Integer precision = 2;
    private Integer order = -1;
    private Boolean nullable = Boolean.FALSE;
    private Boolean primaryKey = Boolean.FALSE;
    private Boolean foreignKey = Boolean.FALSE;
    private String reference;
    private String refCol;
    
    public Column(String name) {
        this.name = name;
    }

    public Column(String name, String type, Integer length, Integer order) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Boolean getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(Boolean foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRefCol() {
        return refCol;
    }

    public void setRefCol(String refCol) {
        this.refCol = refCol;
    }

    public Column cloneMe() {
        try {
            Column col = (Column)super.clone();
            return col;
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final Column other = (Column) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
