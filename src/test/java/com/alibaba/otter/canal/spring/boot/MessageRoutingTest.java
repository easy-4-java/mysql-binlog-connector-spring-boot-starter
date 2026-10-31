package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.annotation.CanalTable;
import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.factory.IModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.impl.RowDataHandlerImpl;
import com.alibaba.otter.canal.handler.impl.SyncMessageHandlerImpl;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MessageRoutingTest {

    @CanalTable(schema = "app", table = "books")
    static class BookListener implements EntryHandler<String> {
        String inserted;
        @Override public void insert(String value) { inserted = value; }
    }

    @SuppressWarnings("unchecked")
    @Test
    void dispatchesMatchingProtobufRowAndClearsContext() throws Exception {
        BookListener listener = new BookListener();
        IModelFactory<List<CanalEntry.Column>> factory = mock(IModelFactory.class);
        when(factory.newInstance(eq(listener), any(List.class))).thenReturn("book-1");
        SyncMessageHandlerImpl handler = new SyncMessageHandlerImpl(
                List.of(listener), new RowDataHandlerImpl(factory));
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("id").setValue("book-1").build();
        CanalEntry.RowData row = CanalEntry.RowData.newBuilder().addAfterColumns(column).build();
        CanalEntry.RowChange change = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.INSERT).addRowDatas(row).build();
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .setSchemaName("app").setTableName("books").setExecuteTime(50).buildPartial();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(header).setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(change.toByteString()).buildPartial();

        handler.handleMessage("orders", new Message(1, List.of(entry)));

        assertThat(listener.inserted).isEqualTo("book-1");
        assertThat(CanalContext.getModel()).isNull();
        verify(factory).newInstance(eq(listener), any(List.class));
    }
}
