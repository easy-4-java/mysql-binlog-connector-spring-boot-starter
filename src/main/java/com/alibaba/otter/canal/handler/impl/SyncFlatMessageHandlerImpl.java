package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.handler.AbstractFlatMessageHandler;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;

import java.util.List;
import java.util.Map;

/**
 * Synchronous {@link com.alibaba.otter.canal.handler.AbstractFlatMessageHandler} that processes
 * each {@link FlatMessage} on the calling thread.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class SyncFlatMessageHandlerImpl extends AbstractFlatMessageHandler {

    /**
     * Constructs a new synchronous flat-message handler using default subscribed entry types.
     *
     * @param entryHandlers  the programmatic entry handlers
     * @param rowDataHandler the row-data handler
     */
    public SyncFlatMessageHandlerImpl(List<? extends EntryHandler> entryHandlers,
                                      RowDataHandler<List<Map<String, String>>> rowDataHandler) {
        super(null, entryHandlers, rowDataHandler);
    }

    /**
     * Constructs a new synchronous flat-message handler with the given subscribed entry types.
     *
     * @param subscribeTypes the entry types to subscribe to
     * @param entryHandlers  the programmatic entry handlers
     * @param rowDataHandler the row-data handler
     */
    public SyncFlatMessageHandlerImpl(List<CanalEntry.EntryType> subscribeTypes,
                                      List<? extends EntryHandler> entryHandlers,
                                      RowDataHandler<List<Map<String, String>>> rowDataHandler) {
        super(subscribeTypes, entryHandlers, rowDataHandler);
    }
    /**
     * <p>Handle message.</p>
     * @param destination the destination
     * @param flatMessage the flat message
     */

    @Override
    public void handleMessage(String destination, FlatMessage flatMessage) {
        super.handleMessage(destination, flatMessage);
    }
}
