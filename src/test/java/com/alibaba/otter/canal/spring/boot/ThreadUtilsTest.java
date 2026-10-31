package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.util.ThreadUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadUtilsTest {

    @Test
    void factoriesNameThreadsAndRespectDaemonFlag() throws Exception {
        ThreadFactory numbered = ThreadUtils.newGenericThreadFactory("worker", 3, true);
        Thread first = numbered.newThread(() -> { });
        Thread second = numbered.newThread(() -> { });
        assertThat(first.getName()).isEqualTo("worker_3_1");
        assertThat(second.getName()).isEqualTo("worker_3_2");
        assertThat(first.isDaemon()).isTrue();
        assertThat(ThreadUtils.newGenericThreadFactory("plain").newThread(() -> { }).getName())
                .isEqualTo("plain_1");
        assertThat(ThreadUtils.newGenericThreadFactory("many", 2).newThread(() -> { }).getName())
                .isEqualTo("many_2_1");
        assertThat(ThreadUtils.newThreadFactory("binlog", false).newThread(() -> { }).getName())
                .isEqualTo("Remoting-binlog_1");
        AtomicBoolean ran = new AtomicBoolean();
        Thread direct = ThreadUtils.newThread("direct", () -> ran.set(true), false);
        assertThat(direct.getName()).isEqualTo("direct");
        direct.start();
        direct.join(1000);
        assertThat(ran).isTrue();
        ThreadUtils.shutdownGracefully(direct);
        ThreadUtils.shutdownGracefully((Thread) null);
    }

    @Test
    void executorsRunTasksAndTerminate() throws Exception {
        ExecutorService single = ThreadUtils.newSingleThreadExecutor("single", true);
        ScheduledExecutorService scheduled = ThreadUtils.newSingleThreadScheduledExecutor("timer", true);
        ScheduledExecutorService fixed = ThreadUtils.newFixedThreadScheduledPool(2, "fixed", true);
        ExecutorService pool = ThreadUtils.newThreadPoolExecutor(
                1, 1, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), "pool", true);
        try {
            assertThat(single.submit(() -> Thread.currentThread().getName()).get(2, TimeUnit.SECONDS))
                    .startsWith("Remoting-single");
            assertThat(scheduled.schedule(() -> 4, 0, TimeUnit.MILLISECONDS)
                    .get(2, TimeUnit.SECONDS)).isEqualTo(4);
            assertThat(fixed.schedule(() -> 5, 0, TimeUnit.MILLISECONDS)
                    .get(2, TimeUnit.SECONDS)).isEqualTo(5);
            assertThat(pool.submit(() -> 6).get(2, TimeUnit.SECONDS)).isEqualTo(6);
        } finally {
            ThreadUtils.shutdownGracefully(single, 2, TimeUnit.SECONDS);
            ThreadUtils.shutdownGracefully(scheduled, 2, TimeUnit.SECONDS);
            ThreadUtils.shutdownGracefully(fixed, 2, TimeUnit.SECONDS);
            ThreadUtils.shutdownGracefully(pool, 2, TimeUnit.SECONDS);
        }
        assertThat(single.isTerminated()).isTrue();
        assertThat(scheduled.isTerminated()).isTrue();
        assertThat(fixed.isTerminated()).isTrue();
        assertThat(pool.isTerminated()).isTrue();
    }
}
