package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.handler.AbstractMessageHandler;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;

import java.util.List;

/**
 * Synchronous {@link com.alibaba.otter.canal.handler.AbstractMessageHandler} that processes
 * each {@link Message} on the calling thread.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class SyncMessageHandlerImpl extends AbstractMessageHandler {


    /**
     * Constructs a new synchronous message handler using default subscribed entry types.
     *
     * @param entryHandlers  the programmatic entry handlers
     * @param rowDataHandler the row-data handler
     */
    public SyncMessageHandlerImpl(List<? extends EntryHandler> entryHandlers,
                                  RowDataHandler<CanalEntry.RowData> rowDataHandler) {
        super(null, entryHandlers, rowDataHandler);
    }

    /**
     * Constructs a new synchronous message handler with the given subscribed entry types.
     *
     * @param subscribeTypes the entry types to subscribe to
     * @param entryHandlers  the programmatic entry handlers
     * @param rowDataHandler the row-data handler
     */
    public SyncMessageHandlerImpl(List<CanalEntry.EntryType> subscribeTypes,
                                  List<? extends EntryHandler> entryHandlers,
                                  RowDataHandler<CanalEntry.RowData> rowDataHandler) {
        super(subscribeTypes, entryHandlers, rowDataHandler);
    }

    @Override
    public void handleMessage(String destination, Message message) {
        super.handleMessage(destination, message);
    }


}
