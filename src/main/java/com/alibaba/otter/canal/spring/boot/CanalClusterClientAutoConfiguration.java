package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.client.ClusterCanalClient;
import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.factory.EntryColumnModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.MessageHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.handler.impl.AsyncMessageHandlerImpl;
import com.alibaba.otter.canal.handler.impl.RowDataHandlerImpl;
import com.alibaba.otter.canal.handler.impl.SyncMessageHandlerImpl;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.util.ConnectorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Auto-configuration for the Canal cluster-mode client. Registers a {@link RowDataHandler},
 * a synchronous or asynchronous {@link MessageHandler} depending on the
 * {@code canal.async} property, and the {@link ClusterCanalClient} that connects to a
 * Canal cluster (direct addresses or ZooKeeper).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass({ SimpleCanalConnector.class, ClusterCanalConnector.class })
@ConditionalOnProperty(value = CanalProperties.CANAL_MODE, havingValue = "cluster")
@EnableConfigurationProperties({CanalProperties.class, CanalClusterProperties.class})
@Import(CanalThreadPoolAutoConfiguration.class)
@Slf4j
public class CanalClusterClientAutoConfiguration {

    /**
     * Registers the default row-data handler backed by {@link EntryColumnModelFactory}.
     *
     * @return a row-data handler for Canal {@link CanalEntry.RowData}
     */
    @Bean
    public RowDataHandler<CanalEntry.RowData> rowDataHandler() {
        return new RowDataHandlerImpl(new EntryColumnModelFactory());
    }

    /**
     * Registers an asynchronous message handler when {@code canal.async=true} (the default).
     *
     * @param properties             the Canal properties
     * @param rowDataHandler         the row-data handler
     * @param entryHandlerProvider   the available entry handlers
     * @param threadPoolTaskExecutor the executor used for asynchronous processing
     * @return an asynchronous message handler
     */
    @Bean
    @ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "true", matchIfMissing = true)
    public MessageHandler asyncMessageHandler(CanalProperties properties,
                                         RowDataHandler<CanalEntry.RowData> rowDataHandler,
                                         ObjectProvider<EntryHandler> entryHandlerProvider,
                                         ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        return new AsyncMessageHandlerImpl(properties.getSubscribeTypes(), entryHandlerProvider.stream().collect(Collectors.toList()), rowDataHandler, threadPoolTaskExecutor);
    }

    /**
     * Registers a synchronous message handler when {@code canal.async=false}.
     *
     * @param properties           the Canal properties
     * @param rowDataHandler       the row-data handler
     * @param entryHandlerProvider the available entry handlers
     * @return a synchronous message handler
     */
    @Bean
    @ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "false")
    public MessageHandler syncMessageHandler(CanalProperties properties,
                                         RowDataHandler<CanalEntry.RowData> rowDataHandler,
                                         ObjectProvider<EntryHandler> entryHandlerProvider) {
        return new SyncMessageHandlerImpl(properties.getSubscribeTypes(), entryHandlerProvider.stream().collect(Collectors.toList()), rowDataHandler);
    }

    /**
     * Creates the {@link ClusterCanalClient} bean, combining connectors defined in the
     * Spring context with any additional connectors declared in the configuration.
     *
     * @param connectorProvider    the cluster connectors registered in the context
     * @param messageHandlerProvider the message handler to use
     * @param canalProperties      the Canal client properties
     * @param connectorProperties  the cluster connector properties
     * @return the cluster Canal client
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public ClusterCanalClient clusterCanalClient(ObjectProvider<ClusterCanalConnector> connectorProvider,
                                                 ObjectProvider<MessageHandler> messageHandlerProvider,
                                                 CanalProperties canalProperties,
                                                 CanalClusterProperties connectorProperties){
        // 1. Collect all ClusterCanalConnector beans from the Spring context
        List<ClusterCanalConnector> clusterCanalConnectors = connectorProvider.stream().collect(Collectors.toList());
        // 2. Initialize additional ClusterCanalConnectors declared in the configuration
        if(!CollectionUtils.isEmpty(connectorProperties.getInstances())){
            clusterCanalConnectors.addAll(connectorProperties.getInstances().stream()
                    .map(instance -> ConnectorUtil.createClusterCanalConnector(instance))
                    .collect(Collectors.toList()));
        }
        // 3. Build and return the ClusterCanalClient
        return (ClusterCanalClient) new ClusterCanalClient.Builder()
                .batchSize(canalProperties.getBatchSize())
                .filter(canalProperties.getFilter())
                .timeout(canalProperties.getTimeout())
                .unit(canalProperties.getUnit())
                .messageHandler(messageHandlerProvider.getIfAvailable())
                .build(clusterCanalConnectors);
    }

}
