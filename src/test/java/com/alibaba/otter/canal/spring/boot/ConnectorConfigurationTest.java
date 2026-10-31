package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.client.kafka.KafkaCanalConnector;
import com.alibaba.otter.canal.client.kafka.KafkaOffsetCanalConnector;
import com.alibaba.otter.canal.client.pulsarmq.PulsarMQCanalConnector;
import com.alibaba.otter.canal.client.rabbitmq.RabbitMQCanalConnector;
import com.alibaba.otter.canal.client.rocketmq.RocketMQCanalConnector;
import com.alibaba.otter.canal.util.ConnectorUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.util.Properties;

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

    @Test
    void kafkaEarliestFlagChoosesOffsetConnectorWithoutCreatingConsumer() {
        CanalKafkaClientProperties.Instance properties = new CanalKafkaClientProperties.Instance();
        properties.setServers("127.0.0.1:9092");
        properties.setTopic("orders");
        properties.setGroupId("local");
        properties.setEarliest(true);
        KafkaCanalConnector earliest = ConnectorUtil.createKafkaCanalConnector(properties);
        assertThat(earliest).isInstanceOf(KafkaOffsetCanalConnector.class);
        assertThat(((Properties) ReflectionTestUtils.getField(earliest, "properties"))
                .getProperty("auto.offset.reset")).isEqualTo("earliest");

        properties.setEarliest(false);
        properties.setBatchSize(7);
        KafkaCanalConnector latest = ConnectorUtil.createKafkaCanalConnector(properties);
        assertThat(latest).isExactlyInstanceOf(KafkaCanalConnector.class);
        assertThat(((Properties) ReflectionTestUtils.getField(latest, "properties"))
                .getProperty("max.poll.records")).isEqualTo("7");
    }

    @Test
    void brokerConnectorFactoriesPreserveConfiguredModesWithoutConnecting() {
        CanalPulsarClientProperties.Instance pulsar = new CanalPulsarClientProperties.Instance();
        pulsar.setServiceUrl("pulsar://127.0.0.1:6650");
        pulsar.setSubscriptName("local");
        pulsar.setTopic("orders");
        assertThat(ConnectorUtil.createPulsarMQCanalConnector(pulsar))
                .isInstanceOf(PulsarMQCanalConnector.class);

        CanalRabbitmqClientProperties.Instance rabbit = new CanalRabbitmqClientProperties.Instance();
        rabbit.setAddresses("127.0.0.1");
        rabbit.setQueueName("orders");
        assertThat(ConnectorUtil.createRabbitMQCanalConnector(rabbit))
                .isInstanceOf(RabbitMQCanalConnector.class);
    }

    @Test
    void rocketMqFactoryChoosesCredentialAndTraceVariantsWithoutConnecting() {
        CanalRocketmqClientProperties.Instance properties = new CanalRocketmqClientProperties.Instance();
        properties.setNameServer("127.0.0.1:9876");
        properties.setTopic("orders");
        properties.setGroupName("local");
        RocketMQCanalConnector plain = ConnectorUtil.createRocketMQCanalConnector(properties);
        assertThat(plain).isNotNull();
        assertThat(ReflectionTestUtils.getField(plain, "accessKey")).isNull();

        properties.setAccessKey("local-key");
        properties.setSecretKey("local-secret");
        RocketMQCanalConnector credentialed = ConnectorUtil.createRocketMQCanalConnector(properties);
        assertThat(ReflectionTestUtils.getField(credentialed, "accessKey")).isEqualTo("local-key");
        properties.setCustomizedTraceTopic("local-trace");
        RocketMQCanalConnector traced = ConnectorUtil.createRocketMQCanalConnector(properties);
        assertThat(ReflectionTestUtils.getField(traced, "customizedTraceTopic"))
                .isEqualTo("local-trace");
        properties.setNamespace("local-namespace");
        RocketMQCanalConnector namespaced = ConnectorUtil.createRocketMQCanalConnector(properties);
        assertThat(ReflectionTestUtils.getField(namespaced, "namespace"))
                .isEqualTo("local-namespace");
    }
}

