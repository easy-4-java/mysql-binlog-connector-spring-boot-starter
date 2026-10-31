package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.annotation.CanalTable;
import com.alibaba.otter.canal.factory.IModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.impl.AsyncFlatMessageHandlerImpl;
import com.alibaba.otter.canal.handler.impl.AsyncMessageHandlerImpl;
import com.alibaba.otter.canal.handler.impl.MapRowDataHandlerImpl;
import com.alibaba.otter.canal.handler.impl.RowDataHandlerImpl;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
import com.alibaba.otter.canal.protocol.Message;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AsyncRoutingTest {

    @CanalTable(schema = "app", table = "books")
    static class Listener implements EntryHandler<String> {
        final CountDownLatch delivered = new CountDownLatch(1);
        volatile String inserted;
        @Override public void insert(String value) {
            inserted = value;
            delivered.countDown();
        }
    }

    private ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();
        return executor;
    }

    @SuppressWarnings("unchecked")
    @Test
    void protobufMessageRunsOnExecutorAndReachesMatchingListener() throws Exception {
        Listener listener = new Listener();
        IModelFactory<List<CanalEntry.Column>> factory = mock(IModelFactory.class);
        when(factory.newInstance(eq(listener), any(List.class))).thenReturn("book-1");
        CanalEntry.RowData row = CanalEntry.RowData.newBuilder()
                .addAfterColumns(CanalEntry.Column.newBuilder().setName("id")
                        .setValue("book-1").build()).build();
        CanalEntry.RowChange change = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.INSERT).addRowDatas(row).build();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(CanalEntry.Header.newBuilder()
                        .setSchemaName("app").setTableName("books").buildPartial())
                .setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(change.toByteString()).buildPartial();
        ThreadPoolTaskExecutor executor = executor();
        try {
            new AsyncMessageHandlerImpl(List.of(listener), new RowDataHandlerImpl(factory),
                    executor).handleMessage("orders", new Message(1, List.of(entry)));
            assertThat(listener.delivered.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(listener.inserted).isEqualTo("book-1");
        } finally {
            executor.shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void flatMessageRunsOnExecutorAndReachesMatchingListener() throws Exception {
        Listener listener = new Listener();
        IModelFactory<Map<String, String>> factory = mock(IModelFactory.class);
        Map<String, String> row = Map.of("id", "book-2");
        when(factory.newInstance(listener, row)).thenReturn("book-2");
        FlatMessage message = new FlatMessage(2);
        message.setDatabase("app");
        message.setTable("books");
        message.setType("INSERT");
        message.setData(List.of(row));
        ThreadPoolTaskExecutor executor = executor();
        try {
            new AsyncFlatMessageHandlerImpl(List.of(listener), new MapRowDataHandlerImpl(factory),
                    executor).handleMessage("orders", message);
            assertThat(listener.delivered.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(listener.inserted).isEqualTo("book-2");
        } finally {
            executor.shutdown();
        }
    }
}
