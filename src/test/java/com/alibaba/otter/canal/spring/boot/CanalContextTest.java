package com.alibaba.otter.canal.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.model.CanalModel;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CanalContext}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class CanalContextTest {

    @Test
    void setAndGetModel() {
        CanalModel model = CanalModel.builder()
                .id(1L)
                .schema("testdb")
                .table("users")
                .build();

        CanalContext.setModel(model);
        CanalModel retrieved = CanalContext.getModel();
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(1L);
        assertThat(retrieved.getSchema()).isEqualTo("testdb");
    }

    @Test
    void removeModel() {
        CanalModel model = CanalModel.builder().id(2L).build();
        CanalContext.setModel(model);
        CanalContext.removeModel();
        assertThat(CanalContext.getModel()).isNull();
    }
}
