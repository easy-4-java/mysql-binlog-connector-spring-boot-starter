package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.util.ConnectorUtil;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectorConfigurationTest {

    @Test
    void simpleConnectorCarriesServerAddressAndTimeoutsWithoutConnecting() {
        CanalSimpleProperties.Instance properties = new CanalSimpleProperties.Instance();
        properties.setHost("127.0.0.1");
        properties.setPort(11112);
        properties.setDestination("orders");
        properties.setUsername("local-user");
        properties.setPassword("local-pass");
        properties.setSoTimeout(321);
        properties.setIdleTimeout(654);

        SimpleCanalConnector connector = ConnectorUtil.createSimpleCanalConnector(properties);

        assertThat(connector.getAddress()).isEqualTo(new InetSocketAddress("127.0.0.1", 11112));
        assertThat(connector.getUsername()).isEqualTo("local-user");
        assertThat(connector.getPassword()).isEqualTo("local-pass");
        assertThat(connector.getSoTimeout()).isEqualTo(321);
        assertThat(connector.getIdleTimeout()).isEqualTo(654);
    }

    @Test
    void clusterConnectorUsesConfiguredAddressesAndRetryPolicyWithoutConnecting() {
        CanalClusterProperties.Instance properties = new CanalClusterProperties.Instance();
        properties.setAddresses("127.0.0.1:11111,127.0.0.2:11112");
        properties.setDestination("orders");
        properties.setUsername("local-user");
        properties.setPassword("local-pass");
        properties.setSoTimeout(321);
        properties.setIdleTimeout(654);
        properties.setRetryTimes(2);
        properties.setRetryInterval(50);

        ClusterCanalConnector connector = ConnectorUtil.createClusterCanalConnector(properties);

        assertThat(connector.getAccessStrategy()).isNotNull();
        assertThat(connector.getUsername()).isEqualTo("local-user");
        assertThat(connector.getPassword()).isEqualTo("local-pass");
        assertThat(connector.getSoTimeout()).isEqualTo(321);
        assertThat(connector.getIdleTimeout()).isEqualTo(654);
        assertThat(connector.getRetryTimes()).isEqualTo(2);
        assertThat(connector.getRetryInterval()).isEqualTo(50);
    }
}
