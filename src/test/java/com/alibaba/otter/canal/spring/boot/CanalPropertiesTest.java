package com.alibaba.otter.canal.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Tests for Canal properties classes.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class CanalPropertiesTest {

    @Test
    void canalProperties_defaultValues() {
        CanalProperties props = new CanalProperties();
        assertThat(props.getMode()).isEqualTo(CanalProperties.ClientMode.simple);
        assertThat(props.getAsync()).isNull();
        assertThat(props.getFilter()).isEmpty();
        assertThat(props.getBatchSize()).isEqualTo(1000);
        assertThat(props.getTimeout()).isEqualTo(-1L);
        assertThat(props.getUnit()).isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    void canalProperties_setterGetter() {
        CanalProperties props = new CanalProperties();
        props.setMode(CanalProperties.ClientMode.cluster);
        props.setAsync(true);
        props.setFilter(".*\\..*");
        props.setBatchSize(500);
        props.setTimeout(5000L);
        props.setUnit(TimeUnit.MILLISECONDS);

        assertThat(props.getMode()).isEqualTo(CanalProperties.ClientMode.cluster);
        assertThat(props.getAsync()).isTrue();
        assertThat(props.getFilter()).isEqualTo(".*\\..*");
        assertThat(props.getBatchSize()).isEqualTo(500);
        assertThat(props.getTimeout()).isEqualTo(5000L);
        assertThat(props.getUnit()).isEqualTo(TimeUnit.MILLISECONDS);
    }

    @Test
    void clientMode_values() {
        CanalProperties.ClientMode[] modes = CanalProperties.ClientMode.values();
        assertThat(modes).hasSize(6);
        assertThat(modes).contains(
                CanalProperties.ClientMode.simple,
                CanalProperties.ClientMode.cluster,
                CanalProperties.ClientMode.kafka
        );
    }

    @Test
    void prefix() {
        assertThat(CanalProperties.PREFIX).isEqualTo("canal");
    }

    @Test
    void simpleProperties_defaultValues() {
        CanalSimpleProperties props = new CanalSimpleProperties();
        assertThat(props).isNotNull();
    }

    @Test
    void clusterProperties_defaultValues() {
        CanalClusterProperties props = new CanalClusterProperties();
        assertThat(props).isNotNull();
    }

    @Test
    void threadPoolProperties_defaultValues() {
        CanalThreadPoolProperties props = new CanalThreadPoolProperties();
        assertThat(props).isNotNull();
    }
}
