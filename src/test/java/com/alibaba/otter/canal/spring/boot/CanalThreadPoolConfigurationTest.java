package com.alibaba.otter.canal.spring.boot;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CanalThreadPoolConfigurationTest {

    @Test
    void configuredExecutorRunsTasksAndUsesCanalThreadNames() throws Exception {
        CanalThreadPoolProperties properties = new CanalThreadPoolProperties();
        properties.setCorePoolSize(1);
        properties.setMaxPoolSize(2);
        properties.setQueueCapacity(2);
        properties.setKeepAlive(Duration.ofSeconds(3));
        properties.setRejectedPolicy(CanalThreadPoolProperties.RejectedPolicy.CallerRunsPolicy);
        ThreadPoolTaskExecutor executor =
                new CanalThreadPoolAutoConfiguration().canalTaskExecutor(properties);
        try {
            AtomicReference<String> threadName = new AtomicReference<>();
            executor.submit(() -> threadName.set(Thread.currentThread().getName()))
                    .get(2, TimeUnit.SECONDS);
            assertThat(threadName.get()).startsWith("canal-execute-thread-");
            assertThat(executor.getCorePoolSize()).isEqualTo(1);
            assertThat(executor.getMaxPoolSize()).isEqualTo(2);
            assertThat(executor.getQueueCapacity()).isEqualTo(2);
        } finally {
            executor.shutdown();
        }
    }
}
