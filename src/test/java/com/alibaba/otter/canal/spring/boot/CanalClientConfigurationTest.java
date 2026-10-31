package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.client.ClusterCanalClient;
import com.alibaba.otter.canal.client.SimpleCanalClient;
import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.handler.MessageHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CanalClientConfigurationTest {

    @SuppressWarnings("unchecked")
    @Test
    void simpleClientIncludesConfiguredConnectorAndMessageHandlerWithoutStarting() {
        CanalSimpleProperties.Instance instance = new CanalSimpleProperties.Instance();
        instance.setHost("127.0.0.1");
        instance.setPort(11112);
        instance.setDestination("orders");
        CanalSimpleProperties connectorProperties = new CanalSimpleProperties();
        connectorProperties.setInstances(List.of(instance));
        ObjectProvider<SimpleCanalConnector> provider = mock(ObjectProvider.class);
        when(provider.stream()).thenReturn(Stream.empty());
        MessageHandler handler = mock(MessageHandler.class);

        SimpleCanalClient client = new CanalSimpleClientAutoConfiguration()
                .simpleCanalClient(provider, handler, new CanalProperties(), connectorProperties);

        assertThat(client.getMessageHandler()).isSameAs(handler);
        List<?> connectors = (List<?>) ReflectionTestUtils.getField(client, "connectors");
        assertThat(connectors).hasSize(1);
        assertThat(((SimpleCanalConnector) connectors.get(0)).getAddress())
                .isEqualTo(new InetSocketAddress("127.0.0.1", 11112));
    }

    @SuppressWarnings("unchecked")
    @Test
    void clusterClientIncludesConfiguredAddressesAndMessageHandlerWithoutStarting() {
        CanalClusterProperties.Instance instance = new CanalClusterProperties.Instance();
        instance.setAddresses("127.0.0.1:11111,127.0.0.2:11112");
        instance.setDestination("orders");
        CanalClusterProperties connectorProperties = new CanalClusterProperties();
        connectorProperties.setInstances(List.of(instance));
        ObjectProvider<ClusterCanalConnector> provider = mock(ObjectProvider.class);
        when(provider.stream()).thenReturn(Stream.empty());
        ObjectProvider<MessageHandler> handlers = mock(ObjectProvider.class);
        MessageHandler handler = mock(MessageHandler.class);
        when(handlers.getIfAvailable()).thenReturn(handler);

        ClusterCanalClient client = new CanalClusterClientAutoConfiguration()
                .clusterCanalClient(provider, handlers, new CanalProperties(), connectorProperties);

        assertThat(client.getMessageHandler()).isSameAs(handler);
        List<?> connectors = (List<?>) ReflectionTestUtils.getField(client, "connectors");
        assertThat(connectors).hasSize(1);
        ClusterCanalConnector connector = (ClusterCanalConnector) connectors.get(0);
        assertThat(connector.getAccessStrategy()).isNotNull();
        assertThat(connector.getRetryTimes()).isEqualTo(instance.getRetryTimes());
    }
}
