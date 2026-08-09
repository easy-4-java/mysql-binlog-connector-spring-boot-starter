package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.alibaba.otter.canal.client.impl.ClusterNodeAccessStrategy;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleNodeAccessStrategy;
import com.alibaba.otter.canal.client.kafka.KafkaCanalConnector;
import com.alibaba.otter.canal.client.kafka.KafkaOffsetCanalConnector;
import com.alibaba.otter.canal.client.pulsarmq.PulsarMQCanalConnector;
import com.alibaba.otter.canal.client.rabbitmq.RabbitMQCanalConnector;
import com.alibaba.otter.canal.client.rocketmq.RocketMQCanalConnector;
import com.alibaba.otter.canal.common.zookeeper.ZkClientx;
import com.alibaba.otter.canal.spring.boot.*;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;

/**
 * Factory methods for creating Canal connectors (simple, cluster, Kafka, PulsarMQ, RabbitMQ
 * and RocketMQ) from the corresponding starter properties.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class ConnectorUtil {

    /**
     * Creates a cluster-mode Canal connector from the given instance configuration.
     *
     * @param instance the cluster instance configuration
     * @return a cluster-mode Canal connector
     */
    public static ClusterCanalConnector createClusterCanalConnector(CanalClusterProperties.Instance instance) {
        if (StringUtils.hasText(instance.getZkServers())) {
            ClusterCanalConnector canalConnector = new ClusterCanalConnector(instance.getUsername(),
                    instance.getPassword(),
                    instance.getDestination(),
                    new ClusterNodeAccessStrategy(instance.getDestination(), ZkClientx.getZkClient(instance.getZkServers())));
            canalConnector.setSoTimeout(instance.getSoTimeout());
            canalConnector.setIdleTimeout(instance.getIdleTimeout());
            canalConnector.setRetryTimes(instance.getRetryTimes());
            canalConnector.setRetryInterval(instance.getRetryInterval());
            return canalConnector;
        }
        ClusterCanalConnector canalConnector = new ClusterCanalConnector(
                instance.getUsername(),
                instance.getPassword(),
                instance.getDestination(),
                new SimpleNodeAccessStrategy(AddressUtils.parseAddresses(instance.getAddresses())));
        canalConnector.setSoTimeout(instance.getSoTimeout());
        canalConnector.setIdleTimeout(instance.getIdleTimeout());
        canalConnector.setRetryTimes(instance.getRetryTimes());
        canalConnector.setRetryInterval(instance.getRetryInterval());
        return canalConnector;
    }

    /**
     * Creates a Kafka Canal connector from the given instance configuration.
     *
     * @param instance the Kafka instance configuration
     * @return a Kafka Canal connector
     */
    public static KafkaCanalConnector createKafkaCanalConnector(CanalKafkaClientProperties.Instance instance) {
        KafkaCanalConnector connector = instance.isEarliest() ? new KafkaOffsetCanalConnector(instance.getServers(),
                instance.getTopic(),  instance.getPartition(), instance.getGroupId(),
                Boolean.TRUE) : new KafkaCanalConnector(instance.getServers(),
                instance.getTopic(),  instance.getPartition(), instance.getGroupId(),
                instance.getBatchSize(), Boolean.TRUE);
        return connector;
    }

    /**
     * Creates a PulsarMQ Canal connector from the given instance configuration.
     *
     * @param instance the PulsarMQ instance configuration
     * @return a PulsarMQ Canal connector
     */
    public static PulsarMQCanalConnector createPulsarMQCanalConnector(CanalPulsarClientProperties.Instance instance) {
        PulsarMQCanalConnector connector = new PulsarMQCanalConnector(Boolean.TRUE,
                instance.getServiceUrl(), instance.getRoleToken(), instance.getTopic(),
                instance.getSubscriptName(), instance.getBatchSize(), instance.getBatchTimeoutSeconds(),
                instance.getBatchProcessTimeoutSeconds(), instance.getRedeliveryDelaySeconds(),
                instance.getAckTimeoutSeconds(),
                instance.isRetry(), instance.isRetryDLQUpperCase(), instance.getMaxRedeliveryCount());
        return connector;
    }

    /**
     * Creates a RabbitMQ Canal connector from the given instance configuration.
     *
     * @param instance the RabbitMQ instance configuration
     * @return a RabbitMQ Canal connector
     */
    public static RabbitMQCanalConnector createRabbitMQCanalConnector(CanalRabbitmqClientProperties.Instance instance) {
        RabbitMQCanalConnector connector = new RabbitMQCanalConnector(instance.getAddresses(), instance.getVhost(),
                instance.getQueueName(), instance.getAccessKey(), instance.getSecretKey(),
                instance.getUsername(), instance.getPassword(), instance.getResourceOwnerId(),
                Boolean.TRUE);
        return connector;
    }

    /**
     * Creates a RocketMQ Canal connector from the given instance configuration.
     *
     * @param instance the RocketMQ instance configuration
     * @return a RocketMQ Canal connector
     */
    public static RocketMQCanalConnector createRocketMQCanalConnector(CanalRocketmqClientProperties.Instance instance) {
        // 1. Create the connector instance
        RocketMQCanalConnector connector;
        if (StringUtils.hasText(instance.getAccessKey()) && StringUtils.hasText(instance.getSecretKey())) {
            if (StringUtils.hasText(instance.getNamespace())) {
                connector = new RocketMQCanalConnector(instance.getNameServer(), instance.getTopic(),
                        instance.getGroupName(), instance.getAccessKey(), instance.getSecretKey(),
                        instance.getBatchSize(), Boolean.TRUE, instance.isEnableMessageTrace(), null,
                        instance.getAccessChannel(), instance.getNamespace());
            } else if (StringUtils.hasText(instance.getCustomizedTraceTopic())) {
                connector = new RocketMQCanalConnector(instance.getNameServer(), instance.getTopic(),
                        instance.getGroupName(), instance.getAccessKey(), instance.getSecretKey(),
                        instance.getBatchSize(), Boolean.TRUE, instance.isEnableMessageTrace(),
                        instance.getCustomizedTraceTopic(), instance.getAccessChannel());
            } else {
                connector = new RocketMQCanalConnector(instance.getNameServer(), instance.getTopic(),
                        instance.getGroupName(), instance.getAccessKey(), instance.getSecretKey(),
                        instance.getBatchSize(), Boolean.TRUE);
            }
        } else {
            connector = new RocketMQCanalConnector(instance.getNameServer(), instance.getTopic(),
                    instance.getGroupName(), instance.getBatchSize(), Boolean.TRUE);
        }
        return connector;
    }

    /**
     * Creates a simple-mode Canal connector from the given instance configuration.
     *
     * @param instance the simple instance configuration
     * @return a simple-mode Canal connector
     */
    public static SimpleCanalConnector createSimpleCanalConnector(CanalSimpleProperties.Instance instance) {
        InetSocketAddress address = new InetSocketAddress(instance.getHost(), instance.getPort());
        SimpleCanalConnector canalConnector = new SimpleCanalConnector(address,
                instance.getUsername(),
                instance.getPassword(),
                instance.getDestination());
        canalConnector.setSoTimeout(instance.getSoTimeout());
        canalConnector.setIdleTimeout(instance.getIdleTimeout());
        return canalConnector;
    }

}
