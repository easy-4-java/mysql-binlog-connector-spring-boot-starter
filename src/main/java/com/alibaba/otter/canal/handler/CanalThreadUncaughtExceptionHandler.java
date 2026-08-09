package com.alibaba.otter.canal.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link Thread.UncaughtExceptionHandler} implementation that logs uncaught exceptions
 * raised by Canal worker threads.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public class CanalThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("thread "+ t.getName()+" have a exception",e);
    }

}
