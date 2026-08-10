package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Objects;

/**
 * Utility methods for extracting column values from a Canal {@link CanalEntry.RowData}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class RowDataUtil {

    /**
     * Returns the before-change string value of the given column.
     *
     * @param rowData     the Canal row data
     * @param columnName  the column name to look up
     * @return the before-change value, or {@code null} when not found
     */
    public static String getBeforeValue(CanalEntry.RowData rowData, String columnName) {
        if(Objects.isNull(rowData)){
            return null;
        }
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        if(Objects.isNull(beforeColumnsList)){
            return null;
        }
        for (CanalEntry.Column column : beforeColumnsList) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return Objects.toString(column.getValue(), null);
            }
        }
        return null;
    }

    /**
     * Returns the after-change string value of the given column.
     *
     * @param rowData     the Canal row data
     * @param columnName  the column name to look up
     * @return the after-change value, or {@code null} when not found
     */
    public static String getAfterValue(CanalEntry.RowData rowData, String columnName) {
        if(Objects.isNull(rowData)){
            return null;
        }
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        if(Objects.isNull(afterColumnsList)){
            return null;
        }
        for (CanalEntry.Column column : afterColumnsList) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return  Objects.toString(column.getValue(), null);
            }
        }
        return null;
    }

    /**
     * Returns the value of the given column, preferring the before-change value and falling
     * back to the after-change value.
     *
     * @param rowData     the Canal row data
     * @param columnName  the column name to look up
     * @return the column value, or {@code null} when not found
     */
    public static String getValue(CanalEntry.RowData rowData, String columnName) {
        String value = getBeforeValue(rowData, columnName);
        if(Objects.isNull(value)){
            return getAfterValue(rowData, columnName);
        }
        return value;
    }

}
