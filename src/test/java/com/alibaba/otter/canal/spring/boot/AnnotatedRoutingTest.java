package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.annotation.BinlogEventHandler;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.handler.impl.SyncMessageHandlerImpl;
import com.alibaba.otter.canal.handler.impl.SyncFlatMessageHandlerImpl;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
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
        List<Map<String, String>> receivedRows;

        @OnCanalEvent(destination = "orders", schema = "app", table = "books",
                eventType = {CanalEntry.EventType.INSERT})
        public void onInsert(CanalModel model, CanalEntry.RowChange change,
                             CanalEntry.EventType type) {
            receivedModel = model;
            receivedChange = change;
            receivedType = type;
            assertThat(CanalContext.getModel()).isSameAs(model);
        }

        @OnCanalEvent(destination = "orders", schema = "app", table = "books",
                eventType = {CanalEntry.EventType.UPDATE})
        public void onUpdate(CanalModel model, List<Map<String, String>> rows,
                             CanalEntry.EventType type) {
            receivedModel = model;
            receivedRows = rows;
            receivedType = type;
            assertThat(CanalContext.getModel()).isSameAs(model);
        }
    }

    @BinlogEventHandler
    static class WildcardListener {
        int count;

        @OnCanalEvent(eventType = {CanalEntry.EventType.INSERT})
        public void onAnyInsert() {
            count++;
        }
    }

    @BinlogEventHandler
    static class UpperDestinationListener {
        int count;
        @OnCanalEvent(destination = "Orders", schema = "app", table = "books",
                eventType = {CanalEntry.EventType.INSERT})
        public void onInsert() { count++; }
    }

    @BinlogEventHandler
    static class LowerDestinationListener {
        int count;
        @OnCanalEvent(destination = "orders", schema = "app", table = "books",
                eventType = {CanalEntry.EventType.INSERT})
        public void onInsert() { count++; }
    }

    interface GenericSink<T> {
        void onInsert(T value);
    }

    @BinlogEventHandler
    static class GenericListener implements GenericSink<CanalModel> {
        int count;
        CanalModel received;

        @Override
        @OnCanalEvent(eventType = {CanalEntry.EventType.INSERT})
        public void onInsert(CanalModel model) {
            count++;
            received = model;
        }
    }

    @Test
    void genericBridgeDoesNotDuplicateProtobufOrFlatDelivery() {
        GenericListener listener = new GenericListener();
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansWithAnnotation(BinlogEventHandler.class))
                .thenReturn(Map.of("generic", listener));
        CanalEntry.RowChange change = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.INSERT).build();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(CanalEntry.Header.newBuilder()
                        .setSchemaName("app").setTableName("books").buildPartial())
                .setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(change.toByteString()).buildPartial();

        SyncMessageHandlerImpl protobuf = new SyncMessageHandlerImpl(
                List.of(), mock(RowDataHandler.class));
        protobuf.setApplicationContext(context);
        protobuf.handleMessage("orders", new Message(11, List.of(entry)));
        assertThat(listener.count).isEqualTo(1);
        assertThat(listener.received).isNotNull();
        assertThat(listener.received.getId()).isEqualTo(11);

        FlatMessage flat = new FlatMessage(12);
        flat.setDatabase("app");
        flat.setTable("books");
        flat.setType("INSERT");
        flat.setData(List.of(Map.of("title", "book")));
        flat.setEs(100L);
        flat.setTs(200L);
        SyncFlatMessageHandlerImpl flatHandler = new SyncFlatMessageHandlerImpl(
                List.of(), mock(RowDataHandler.class));
        flatHandler.setApplicationContext(context);
        flatHandler.handleMessage("orders", flat);
        assertThat(listener.count).isEqualTo(2);
        assertThat(listener.received).isNotNull();
        assertThat(listener.received.getId()).isEqualTo(12);
    }

    @Test
    void destinationCaseKeepsSeparateListenersAcrossSequentialEvents() {
        UpperDestinationListener upper = new UpperDestinationListener();
        LowerDestinationListener lower = new LowerDestinationListener();
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansWithAnnotation(BinlogEventHandler.class))
                .thenReturn(Map.of("upper", upper, "lower", lower));
        SyncMessageHandlerImpl handler = new SyncMessageHandlerImpl(
                List.of(), mock(RowDataHandler.class));
        handler.setApplicationContext(context);
        CanalEntry.RowChange change = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.INSERT).build();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(CanalEntry.Header.newBuilder()
                        .setSchemaName("app").setTableName("books").buildPartial())
                .setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(change.toByteString()).buildPartial();

        handler.handleMessage("Orders", new Message(1, List.of(entry)));
        assertThat(upper.count).isEqualTo(1);
        assertThat(lower.count).isZero();
        handler.handleMessage("orders", new Message(2, List.of(entry)));
        assertThat(upper.count).isEqualTo(1);
        assertThat(lower.count).isEqualTo(1);
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

    @Test
    void flatUpdatePassesOldAndNewRowsToAnnotatedListener() {
        Listener listener = new Listener();
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansWithAnnotation(BinlogEventHandler.class))
                .thenReturn(Map.of("listener", listener));
        SyncFlatMessageHandlerImpl handler = new SyncFlatMessageHandlerImpl(
                List.of(), mock(RowDataHandler.class));
        handler.setApplicationContext(context);
        Map<String, String> after = Map.of("title", "new");
        Map<String, String> before = Map.of("title", "old");
        FlatMessage message = new FlatMessage(8);
        message.setDatabase("app");
        message.setTable("books");
        message.setType("UPDATE");
        message.setData(List.of(after));
        message.setOld(List.of(before));
        message.setEs(100L);
        message.setTs(200L);

        handler.handleMessage("orders", message);

        assertThat(listener.receivedRows).containsExactly(after, before);
        assertThat(listener.receivedType).isEqualTo(CanalEntry.EventType.UPDATE);
        assertThat(listener.receivedModel.getId()).isEqualTo(8);
        assertThat(CanalContext.getModel()).isNull();
    }

    @Test
    void defaultWildcardAnnotationReceivesMatchingInsert() {
        WildcardListener listener = new WildcardListener();
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansWithAnnotation(BinlogEventHandler.class))
                .thenReturn(Map.of("listener", listener));
        SyncMessageHandlerImpl handler = new SyncMessageHandlerImpl(
                List.of(), mock(RowDataHandler.class));
        handler.setApplicationContext(context);
        CanalEntry.RowChange change = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.INSERT).build();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(CanalEntry.Header.newBuilder()
                        .setSchemaName("app").setTableName("books").buildPartial())
                .setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(change.toByteString()).buildPartial();

        handler.handleMessage("orders", new Message(9, List.of(entry)));

        assertThat(listener.count).isEqualTo(1);
        assertThat(CanalContext.getModel()).isNull();
    }
}

