package com.alibaba.otter.canal.enums;

import java.util.StringJoiner;

/**
 * Built-in constants for Canal destination, schema and table matching, including the
 * wildcard {@link #ALL} that matches every destination, schema and table.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public enum TableNameEnum {

    /**
     * Wildcard constant matching all destinations, schemas and tables.
     */
    ALL("*", "*", "*");

    /**
     * Delimiter used when joining destination, schema and table into a single key.
     */
    public static final CharSequence DELIMITER = ".";

    /**
     * The destination segment of this constant.
     */
    String destination;
    /**
     * The schema segment of this constant.
     */
    String schema;
    /**
     * The table segment of this constant.
     */
    String table;

    TableNameEnum(String destination, String schema, String table) {
        this.destination = destination;
        this.schema = schema;
        this.table = table;
    }

    /**
     * Returns the destination segment.
     *
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Returns the schema segment.
     *
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Returns the table segment.
     *
     * @return the table
     */
    public String getTable() {
        return table;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(".").add(schema).add(table);
        return joiner.toString();
    }

}
