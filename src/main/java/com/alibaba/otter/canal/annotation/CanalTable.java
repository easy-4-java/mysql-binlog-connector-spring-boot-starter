package com.alibaba.otter.canal.annotation;

import java.lang.annotation.*;

/**
 * Binds an {@link com.alibaba.otter.canal.handler.EntryHandler} to a specific Canal destination,
 * database schema and/or table so that only matching change events are routed to it.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CanalTable {

    /**
     * The Canal destination (canal instance name) to match.
     * Defaults to empty, meaning all destinations.
     *
     * @return the destination name
     */
    String destination() default "";

    /**
     * The database schema (instance) to match.
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

}
