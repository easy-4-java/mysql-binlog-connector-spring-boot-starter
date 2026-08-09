package com.alibaba.otter.canal.annotation;


import com.alibaba.otter.canal.protocol.CanalEntry;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Holds a reference to a Canal event listener bean together with the method to invoke
 * and the matching {@link OnCanalEvent} annotation metadata.
 *
 * @author lujun
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class CanalEventHolder {

    /**
     * The target bean instance that declares the listener method.
     */
    private Object target;
    /**
     * The listener method to be invoked when a matching event arrives.
     */
    private Method method;
    /**
     * The {@link OnCanalEvent} annotation describing the subscribed event.
     */
    private OnCanalEvent event;

    /**
     * Constructs a new holder with the target bean, the listener method and the event annotation.
     *
     * @param target the target bean instance
     * @param method the listener method to invoke
     * @param event  the matching {@link OnCanalEvent} annotation
     */
    public CanalEventHolder(Object target, Method method, OnCanalEvent event) {
        this.target = target;
        this.method = method;
        this.event = event;
    }

    /**
     * Returns the target bean instance.
     *
     * @return the target bean
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Returns the listener method.
     *
     * @return the listener method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the event annotation metadata.
     *
     * @return the {@link OnCanalEvent} annotation
     */
    public OnCanalEvent getEvent() {
        return event;
    }

    /**
     * Returns whether the given event type matches the subscribed event types of this holder.
     *
     * @param eventType the Canal event type to test
     * @return {@code true} if the holder subscribes to all events or to the given event type
     */
    public boolean isMatch(CanalEntry.EventType eventType) {
        return this.getEvent().eventType().length == 0 || Arrays.stream(this.getEvent().eventType()).anyMatch(ev -> ev == eventType) || eventType == null;
    }

}
