package com.alibaba.otter.canal.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Marker annotation for a Canal binlog event handler, meta-annotated with {@link Component}
 * so that annotated beans are registered as Spring components.
 * <p>Apply this annotation to a class that should receive Canal binlog change events.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BinlogEventHandler {

    /**
     * Alias for the {@code value} attribute of {@link Component}, used as the bean name.
     *
     * @return the component name, empty by default
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

}
