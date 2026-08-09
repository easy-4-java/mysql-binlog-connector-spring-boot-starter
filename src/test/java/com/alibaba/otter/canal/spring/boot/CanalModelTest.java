package com.alibaba.otter.canal.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CanalModel}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class CanalModelTest {

    @Test
    void builder_createsModel() {
        CanalModel model = CanalModel.builder()
                .id(1L)
                .schema("testdb")
                .table("users")
                .eventType(CanalEntry.EventType.INSERT)
                .executeTime(System.currentTimeMillis())
                .createTime(System.currentTimeMillis())
                .build();

        assertThat(model.getId()).isEqualTo(1L);
        assertThat(model.getSchema()).isEqualTo("testdb");
        assertThat(model.getTable()).isEqualTo("users");
        assertThat(model.getEventType()).isEqualTo(CanalEntry.EventType.INSERT);
    }

    @Test
    void toString_containsAllFields() {
        CanalModel model = CanalModel.builder()
                .id(1L)
                .schema("testdb")
                .table("users")
                .eventType(CanalEntry.EventType.INSERT)
                .executeTime(1000L)
                .createTime(2000L)
                .build();

        String s = model.toString();
        assertThat(s).contains("id=1");
        assertThat(s).contains("testdb");
        assertThat(s).contains("users");
        assertThat(s).contains("INSERT");
    }

    @Test
    void builder_withAllEventTypes() {
        for (CanalEntry.EventType eventType : CanalEntry.EventType.values()) {
            CanalModel model = CanalModel.builder()
                    .id(1L)
                    .eventType(eventType)
                    .build();
            assertThat(model.getEventType()).isEqualTo(eventType);
        }
    }
}
