package com.alibaba.otter.canal.spring.boot;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.alibaba.otter.canal.handler.CanalThreadUncaughtExceptionHandler;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CanalThreadUncaughtExceptionHandler}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class CanalThreadUncaughtExceptionHandlerTest {

    @Test
    void uncaughtException_handlesGracefully() {
        CanalThreadUncaughtExceptionHandler handler = new CanalThreadUncaughtExceptionHandler();
        Thread thread = new Thread();
        Exception exception = new RuntimeException("test error");
        assertThatCode(() -> handler.uncaughtException(thread, exception))
                .doesNotThrowAnyException();
    }
}
