package com.alibaba.otter.canal.annotation;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.lang.annotation.*;

/**
 * Marks a method as a listener for Canal database change events, optionally
 * filtering by destination, schema, table and event type.
 *
 * @author lujun
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnCanalEvent {

    /**
     * The Canal destination (canal instance name) to subscribe to.
     * Defaults to empty, meaning all destinations.
     *
     * @return the destination name
     */
    String destination() default "";

    /**
     * The database schema (instance) to subscribe to.
     * Defaults to {@code "*"}, meaning all schemas.
     *
     * @return the schema name
     */
    String schema() default "*";

    /**
     * The table to listen on.
     * Defaults to {@code "*"}, meaning all tables.
     *
     * @return the table name
     */
    String table() default "*";

    /**
     * The Canal event types (INSERT, UPDATE, DELETE, etc.) to listen for.
     * Empty array means all event types.
     *
     * @return the matching event types
     */
    CanalEntry.EventType[] eventType();

}
