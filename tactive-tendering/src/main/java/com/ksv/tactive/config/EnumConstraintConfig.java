package com.ksv.tactive.config;

public class EnumConstraintConfig {

    private Class<? extends Enum<?>> enumClass;
    private String tableName;
    private String columnName;
    private String constraintName;

    public EnumConstraintConfig(Class<? extends Enum<?>> enumClass,
                                String tableName,
                                String columnName,
                                String constraintName) {
        this.enumClass = enumClass;
        this.tableName = tableName;
        this.columnName = columnName;
        this.constraintName = constraintName;
    }

    public Class<? extends Enum<?>> getEnumClass() { return enumClass; }
    public String getTableName() { return tableName; }
    public String getColumnName() { return columnName; }
    public String getConstraintName() { return constraintName; }
}
