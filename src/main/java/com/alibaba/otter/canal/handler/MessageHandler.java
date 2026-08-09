package com.alibaba.otter.canal.handler;

/**
 * Functional contract for handling a single Canal message.
 *
 * @param <T> the message type
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@FunctionalInterface
public interface MessageHandler<T> {

    /**
     * Handles the given Canal message.
     *
     * @param destination the Canal destination (canal instance name)
     * @param t           the message to handle
     */
    void handleMessage(String destination, T t);

}
