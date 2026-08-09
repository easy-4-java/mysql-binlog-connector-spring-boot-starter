package com.alibaba.otter.canal.annotation.event;

import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Listener annotation for a {@code DROP TABLE} event; methods annotated with this are
 * invoked when a database table is dropped.
 *
 * @author lujun
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnCanalEvent(eventType = CanalEntry.EventType.ERASE)
public @interface OnDropTableEvent {
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
}
