package com.alibaba.otter.canal.handler;

import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 * Contract for processing a single Canal row-data change and dispatching it to an
 * {@link EntryHandler} based on the event type.
 *
 * @param <T> the row-data type
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface RowDataHandler<T> {

    /**
     * Dispatches the given row data to the appropriate {@link EntryHandler} callback
     * based on the event type (INSERT, UPDATE or DELETE).
     *
     * @param t            the row data to process
     * @param entryHandler the entry handler to invoke
     * @param eventType    the Canal event type
     * @param <R>          the entity type of the entry handler
     * @throws Exception if the handler fails to process the row data
     */
    <R> void handlerRowData(T t, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) throws Exception;

}
