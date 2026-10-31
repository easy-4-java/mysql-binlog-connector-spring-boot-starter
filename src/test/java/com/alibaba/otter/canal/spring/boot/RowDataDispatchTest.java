package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.annotation.CanalTable;
import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.factory.IModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.impl.MapRowDataHandlerImpl;
import com.alibaba.otter.canal.handler.impl.RowDataHandlerImpl;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RowDataDispatchTest {

    @CanalTable(schema = "app", table = "books")
    static class RecordingHandler implements EntryHandler<String> {
        String inserted;
        String before;
        String after;
        String deleted;
        @Override public void insert(String value) { inserted = value; }
        @Override public void update(String oldValue, String newValue) {
            before = oldValue;
            after = newValue;
        }
        @Override public void delete(String value) { deleted = value; }
    }

    private static CanalEntry.Column column(String name, String value, boolean updated) {
        return CanalEntry.Column.newBuilder().setName(name).setValue(value)
                .setUpdated(updated).build();
    }

    @SuppressWarnings("unchecked")
    @Test
    void protobufRowsDispatchInsertUpdateAndDelete() throws Exception {
        IModelFactory<List<CanalEntry.Column>> factory = mock(IModelFactory.class);
        RecordingHandler listener = new RecordingHandler();
        RowDataHandlerImpl dispatch = new RowDataHandlerImpl(factory);
        CanalEntry.Column oldValue = column("title", "old", false);
        CanalEntry.Column newValue = column("title", "new", true);
        CanalEntry.RowData inserted = CanalEntry.RowData.newBuilder()
                .addAfterColumns(newValue).build();
        CanalEntry.RowData updated = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(oldValue).addAfterColumns(newValue).build();
        CanalEntry.RowData deleted = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(oldValue).build();
        when(factory.newInstance(eq(listener), eq(List.of(newValue)))).thenReturn("new");
        when(factory.newInstance(eq(listener), eq(List.of(oldValue)))).thenReturn("old");
        when(factory.newInstance(eq(listener), eq(List.of(oldValue)), eq(Set.of("title"))))
                .thenReturn("old-updated");

        dispatch.handlerRowData(inserted, listener, CanalEntry.EventType.INSERT);
        dispatch.handlerRowData(updated, listener, CanalEntry.EventType.UPDATE);
        dispatch.handlerRowData(deleted, listener, CanalEntry.EventType.DELETE);

        assertThat(listener.inserted).isEqualTo("new");
        assertThat(listener.before).isEqualTo("old-updated");
        assertThat(listener.after).isEqualTo("new");
        assertThat(listener.deleted).isEqualTo("old");
        dispatch.handlerRowData(null, listener, CanalEntry.EventType.INSERT);
        dispatch.handlerRowData(inserted, null, CanalEntry.EventType.INSERT);
        dispatch.handlerRowData(inserted, listener, null);
        assertThat(CanalContext.getModel()).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void flatRowsKeepBeforeAndAfterOrder() throws Exception {
        IModelFactory<Map<String, String>> factory = mock(IModelFactory.class);
        RecordingHandler listener = new RecordingHandler();
        MapRowDataHandlerImpl dispatch = new MapRowDataHandlerImpl(factory);
        Map<String, String> oldValue = Map.of("title", "old");
        Map<String, String> newValue = Map.of("title", "new");
        when(factory.newInstance(listener, oldValue)).thenReturn("old");
        when(factory.newInstance(listener, newValue)).thenReturn("new");

        dispatch.handlerRowData(List.of(newValue), listener, CanalEntry.EventType.INSERT);
        dispatch.handlerRowData(List.of(newValue, oldValue), listener, CanalEntry.EventType.UPDATE);
        dispatch.handlerRowData(List.of(oldValue), listener, CanalEntry.EventType.DELETE);

        assertThat(listener.inserted).isEqualTo("new");
        assertThat(listener.before).isEqualTo("old");
        assertThat(listener.after).isEqualTo("new");
        assertThat(listener.deleted).isEqualTo("old");
        dispatch.handlerRowData(null, listener, CanalEntry.EventType.INSERT);
        dispatch.handlerRowData(List.of(newValue), null, CanalEntry.EventType.INSERT);
        dispatch.handlerRowData(List.of(newValue), listener, null);
        verify(factory, times(4)).newInstance(any(), any(Map.class));
    }
}
