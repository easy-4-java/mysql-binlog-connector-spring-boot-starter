package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.annotation.BinlogEventHandler;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.handler.impl.SyncMessageHandlerImpl;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnnotatedRoutingTest {

    @BinlogEventHandler
    static class Listener {
        CanalModel receivedModel;
        CanalEntry.EventType receivedType;
        CanalEntry.RowChange receivedChange;

        @OnCanalEvent(destination = "orders", schema = "app", table = "books",
                eventType = {CanalEntry.EventType.INSERT})
        public void onInsert(CanalModel model, CanalEntry.RowChange change,
                             CanalEntry.EventType type) {
            receivedModel = model;
            receivedChange = change;
            receivedType = type;
            assertThat(CanalContext.getModel()).isSameAs(model);
        }
    }

    @Test
    void discoversMethodAnnotationAndPassesProtobufEventArguments() {
        Listener listener = new Listener();
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansWithAnnotation(BinlogEventHandler.class))
                .thenReturn(Map.of("listener", listener));
        SyncMessageHandlerImpl handler = new SyncMessageHandlerImpl(List.of(), mock(RowDataHandler.class));
        handler.setApplicationContext(context);

        CanalEntry.RowChange change = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.INSERT).build();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(CanalEntry.Header.newBuilder()
                        .setSchemaName("app").setTableName("books").setExecuteTime(50).buildPartial())
                .setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(change.toByteString()).buildPartial();
        handler.handleMessage("orders", new Message(7, List.of(entry)));

        assertThat(listener.receivedModel.getId()).isEqualTo(7);
        assertThat(listener.receivedModel.getSchema()).isEqualTo("app");
        assertThat(listener.receivedModel.getTable()).isEqualTo("books");
        assertThat(listener.receivedChange).isEqualTo(change);
        assertThat(listener.receivedType).isEqualTo(CanalEntry.EventType.INSERT);
        assertThat(CanalContext.getModel()).isNull();
    }
}
