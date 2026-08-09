package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.handler.AbstractMessageHandler;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

/**
 * Asynchronous {@link com.alibaba.otter.canal.handler.AbstractMessageHandler} that
 * dispatches each {@link Message} to a thread pool for processing.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class AsyncMessageHandlerImpl extends AbstractMessageHandler {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * Constructs a new asynchronous message handler using default subscribed entry types.
     *
     * @param entryHandlers          the programmatic entry handlers
     * @param rowDataHandler         the row-data handler
     * @param threadPoolTaskExecutor the executor used to process messages asynchronously
     */
    public AsyncMessageHandlerImpl(List<? extends EntryHandler> entryHandlers,
                                   RowDataHandler<CanalEntry.RowData> rowDataHandler,
                                   ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super(null, entryHandlers, rowDataHandler);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    /**
     * Constructs a new asynchronous message handler with the given subscribed entry types.
     *
     * @param subscribeTypes         the entry types to subscribe to
     * @param entryHandlers          the programmatic entry handlers
     * @param rowDataHandler         the row-data handler
     * @param threadPoolTaskExecutor the executor used to process messages asynchronously
     */
    public AsyncMessageHandlerImpl(List<CanalEntry.EntryType> subscribeTypes,
                                   List<? extends EntryHandler> entryHandlers,
                                   RowDataHandler<CanalEntry.RowData> rowDataHandler,
                                   ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super(subscribeTypes, entryHandlers, rowDataHandler);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    @Override
    public void handleMessage(String destination, Message message) {
        threadPoolTaskExecutor.execute(() -> super.handleMessage(destination, message));
    }

}
