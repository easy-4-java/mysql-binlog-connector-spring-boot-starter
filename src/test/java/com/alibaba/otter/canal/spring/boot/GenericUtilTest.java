package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.util.GenericUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GenericUtilTest {

    static class StringHandler implements EntryHandler<String> { }

    public void rowListener(CanalModel model, CanalEntry.RowChange row,
                            CanalEntry.EventType eventType, String ignored) { }

    public void flatListener(CanalModel model, List<Map<String, String>> rows,
                             CanalEntry.EventType eventType, String ignored) { }

    @Test
    void resolvesEntryHandlerEntityTypeAndCachesIt() {
        EntryHandler<String> handler = new StringHandler();
        assertThat(GenericUtil.getTableClass(handler)).isEqualTo(String.class);
        assertThat(GenericUtil.getTableClass(handler)).isEqualTo(String.class);
        assertThat(GenericUtil.getTableGenericProperties(handler)).isNull();
    }

    @Test
    void mapsKnownListenerArgumentsInDeclaredOrder() throws Exception {
        CanalModel model = CanalModel.builder().id(5).build();
        CanalEntry.RowChange row = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.INSERT).build();
        Method method = getClass().getMethod("rowListener", CanalModel.class,
                CanalEntry.RowChange.class, CanalEntry.EventType.class, String.class);

        assertThat(GenericUtil.getInvokeArgs(method, model, row, CanalEntry.EventType.INSERT))
                .containsExactly(model, row, CanalEntry.EventType.INSERT, null);
    }

    @Test
    void mapsFlatRowDataListenerArguments() throws Exception {
        CanalModel model = CanalModel.builder().id(7).build();
        List<Map<String, String>> rows = List.of(Map.of("id", "7"));
        Method method = getClass().getMethod("flatListener", CanalModel.class,
                List.class, CanalEntry.EventType.class, String.class);

        assertThat(GenericUtil.getInvokeArgs(method, model, rows, CanalEntry.EventType.UPDATE))
                .containsExactly(model, rows, CanalEntry.EventType.UPDATE, null);
    }
}
