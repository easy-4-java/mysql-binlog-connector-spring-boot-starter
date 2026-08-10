package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.handler.AbstractFlatMessageHandler;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;

/**
 * Asynchronous {@link com.alibaba.otter.canal.handler.AbstractFlatMessageHandler} that
 * dispatches each {@link FlatMessage} to a thread pool for processing.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class AsyncFlatMessageHandlerImpl extends AbstractFlatMessageHandler {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * Constructs a new asynchronous flat-message handler using default subscribed entry types.
     *
     * @param entryHandlers          the programmatic entry handlers
     * @param rowDataHandler         the row-data handler
     * @param threadPoolTaskExecutor the executor used to process messages asynchronously
     */
    public AsyncFlatMessageHandlerImpl(List<? extends EntryHandler> entryHandlers,
                                       RowDataHandler<List<Map<String, String>>> rowDataHandler,
                                       ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super(null, entryHandlers, rowDataHandler);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    /**
     * Constructs a new asynchronous flat-message handler with the given subscribed entry types.
     *
     * @param subscribeTypes         the entry types to subscribe to
     * @param entryHandlers          the programmatic entry handlers
     * @param rowDataHandler         the row-data handler
     * @param threadPoolTaskExecutor the executor used to process messages asynchronously
     */
    public AsyncFlatMessageHandlerImpl(List<CanalEntry.EntryType> subscribeTypes,
                                       List<? extends EntryHandler> entryHandlers,
                                       RowDataHandler<List<Map<String, String>>> rowDataHandler,
                                       ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super(subscribeTypes, entryHandlers, rowDataHandler);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    @Override
    public void handleMessage(String destination, FlatMessage flatMessage) {
        threadPoolTaskExecutor.execute(() -> super.handleMessage(destination, flatMessage));
    }


}
