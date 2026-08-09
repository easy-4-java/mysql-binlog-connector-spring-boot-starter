package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.common.CanalLifeCycle;
import com.alibaba.otter.canal.handler.CanalThreadUncaughtExceptionHandler;
import com.alibaba.otter.canal.protocol.CanalPacket;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Auto-configuration that registers the Canal thread pool task executor
 * (bean name {@code canalTaskExecutor}) used by the asynchronous message handlers.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass({ CanalConnector.class, CanalLifeCycle.class, CanalPacket.class })
@ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "true")
@EnableConfigurationProperties({CanalProperties.class, CanalThreadPoolProperties.class})
public class CanalThreadPoolAutoConfiguration {

    /**
     * Creates the {@code canalTaskExecutor} bean used to process Canal messages asynchronously.
     *
     * @param poolProperties the thread-pool properties
     * @return the configured task executor
     */
    @Bean(destroyMethod = "shutdown", name = "canalTaskExecutor")
    public ThreadPoolTaskExecutor canalTaskExecutor(CanalThreadPoolProperties poolProperties) {
        BasicThreadFactory factory = new BasicThreadFactory.Builder().namingPattern("canal-execute-thread-%d")
                .uncaughtExceptionHandler(new CanalThreadUncaughtExceptionHandler()).build();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadFactory(factory);
        executor.setCorePoolSize(poolProperties.getCorePoolSize());
        executor.setMaxPoolSize(poolProperties.getMaxPoolSize());
        executor.setQueueCapacity(poolProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(Long.valueOf(poolProperties.getKeepAlive().getSeconds()).intValue());
        executor.setAllowCoreThreadTimeOut(poolProperties.isAllowCoreThreadTimeOut());
        executor.setAwaitTerminationSeconds(poolProperties.getAwaitTerminationSeconds());
        executor.setWaitForTasksToCompleteOnShutdown(poolProperties.isWaitForTasksToCompleteOnShutdown());
        executor.setThreadNamePrefix(poolProperties.getThreadNamePrefix());
        // Rejected-execution policies:
        // CallerRunsPolicy()  - run the task on the caller thread (e.g. main thread)
        // AbortPolicy()        - throw a RejectedExecutionException
        // DiscardPolicy()      - silently discard the task
        // DiscardOldestPolicy()- discard the oldest queued task
        executor.setRejectedExecutionHandler(poolProperties.getRejectedPolicy().getRejectedExecutionHandler());
        // Initialize the thread pool
        executor.initialize();
        return executor;
    }

}
