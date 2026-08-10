package com.alibaba.otter.canal.annotation.event;

import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Listener annotation for an {@code UPDATE} event; methods annotated with this are
 * triggered when an update operation occurs.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnCanalEvent(eventType = CanalEntry.EventType.UPDATE)
public @interface OnUpdateEvent {

    /**
     * The Canal destination (canal instance name) to subscribe to.
     * Defaults to empty, meaning all destinations.
     *
     * @return the canal destination
     */
    @AliasFor(annotation = OnCanalEvent.class)
    String destination() default "";

    /**
     * The database schema (instance) to subscribe to.
     *
     * @return the schema name
     */
    @AliasFor(annotation = OnCanalEvent.class)
    String schema();

    /**
     * The table to listen on.
     * Defaults to {@code "*"}, meaning all tables.
     *
     * @return the table name
     */
    @AliasFor(annotation = OnCanalEvent.class)
    String table();

}
