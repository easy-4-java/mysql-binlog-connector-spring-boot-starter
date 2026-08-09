package com.alibaba.otter.canal.model;


import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Model object describing a single Canal change-data message, exposed to event listeners
 * via the thread-local {@link com.alibaba.otter.canal.context.CanalContext}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Setter
@Getter
@Builder
public class CanalModel {


    /**
     * The Canal message/batch id.
     */
    private long id;

    /**
     * The Canal destination (canal instance name).
     */
    private String destination;

    /**
     * The database schema (instance) name.
     */
    private String schema;
    /**
     * The table name.
     */
    private String table;
    /**
     * The Canal event type (INSERT, UPDATE, DELETE, etc.).
     */
    private CanalEntry.EventType eventType;
    /**
     * The original binlog execute time.
     */
    private Long executeTime;

    /**
     * The timestamp at which the DML message was built.
     */
    private Long createTime;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CanalModel{");
        sb.append("id=").append(id);
        sb.append(", schema='").append(schema).append('\'');
        sb.append(", table='").append(table).append('\'');
        sb.append(", eventType='").append(eventType).append('\'');
        sb.append(", executeTime=").append(executeTime);
        sb.append(", createTime=").append(createTime);
        sb.append('}');
        return sb.toString();
    }

}
