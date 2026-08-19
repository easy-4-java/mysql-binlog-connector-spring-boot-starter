package com.alibaba.otter.canal.handler;

import com.alibaba.otter.canal.annotation.BinlogEventHandler;
import com.alibaba.otter.canal.annotation.CanalEventHolder;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.util.GenericUtil;
import com.alibaba.otter.canal.util.HandlerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;


/**
 * Abstract base class for Canal {@link Message} handlers. It parses each {@link CanalEntry.Entry},
 * resolves the matching annotation-based event holders or {@link EntryHandler} instances for each
 * change event and delegates row-data processing to a {@link RowDataHandler}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMessageHandler implements MessageHandler<Message>, ApplicationContextAware {

    /**
     * Subscribed entry types, mainly used to flag transaction begin, row-data change and transaction end.
     */
    private List<CanalEntry.EntryType> subscribeTypes = Arrays.asList(CanalEntry.EntryType.ROWDATA);
    /**
     * Table change event handlers registered via annotations.
     */
    private Map<String, List<CanalEventHolder>> tableEventHolderMap;
    /**
     * Programmatic table change handlers.
     */
    private Map<String, EntryHandler> tableHandlerMap;
    /**
     * The row-data handler used to process each row of a change event.
     */
    private RowDataHandler<CanalEntry.RowData> rowDataHandler;

    /**
     * Constructs a new message handler.
     *
     * @param subscribeTypes the entry types to subscribe to, or {@code null} to use the default
     * @param entryHandlers  the programmatic entry handlers
     * @param rowDataHandler the row-data handler
     */
    public AbstractMessageHandler(List<CanalEntry.EntryType> subscribeTypes,
                                  List<? extends EntryHandler> entryHandlers,
                                  RowDataHandler<CanalEntry.RowData> rowDataHandler) {
        if(Objects.nonNull(subscribeTypes)){
            this.subscribeTypes = subscribeTypes;
        }
        this.tableHandlerMap = HandlerUtil.getTableHandlerMap(entryHandlers);
        this.rowDataHandler = rowDataHandler;
    }

    /**
     * Returns whether the given entry type is subscribed.
     *
     * @param entryType the entry type to test
     * @return {@code true} if the entry type is subscribed
     */
    protected boolean isSubscribed(CanalEntry.EntryType entryType) {
        return subscribeTypes.contains(entryType);
    }
    /**
     * <p>Handle message.</p>
     * @param destination the destination
     * @param message the message
     */

    @Override
    public void handleMessage(String destination, Message message) {
        // Iterate over the entries, parsing one at a time
        for (CanalEntry.Entry entry : message.getEntries()) {
            // Entry type
            CanalEntry.EntryType entryType = entry.getEntryType();
            // Check whether the entry type is subscribed
            if (this.isSubscribed(entryType)) {
                // Database instance (schema) name
                String schemaName = entry.getHeader().getSchemaName();
                // Table name
                String tableName = entry.getHeader().getTableName();
                try {
                    // Deserialize the row change payload
                    CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                    // Event type of the current change
                    CanalEntry.EventType eventType = rowChange.getEventType();
                    // Resolve the annotation-based event holders for the table
                    List<CanalEventHolder> eventHolders = HandlerUtil.getEventHolders(tableEventHolderMap, destination, schemaName, tableName, eventType);
                    if(!CollectionUtils.isEmpty(eventHolders)){
                        CanalModel model = CanalModel.builder()
                                .id(message.getId())
                                .schema(schemaName)
                                .table(tableName)
                                .eventType(eventType)
                                .executeTime(entry.getHeader().getExecuteTime())
                                .build();
                        for (CanalEventHolder eventHolder : eventHolders) {
                            this.handlerRowData(model, rowChange, eventHolder, eventType);
                        }
                        continue;
                    }
                    // Resolve the programmatic entry handler for the table
                    EntryHandler<?> entryHandler = HandlerUtil.getEntryHandler(tableHandlerMap, schemaName, tableName);
                    // Dispatch when a matching handler exists
                    if(Objects.nonNull(entryHandler)){
                        CanalModel model = CanalModel.builder()
                                .id(message.getId())
                                .schema(schemaName)
                                .table(tableName)
                                .eventType(eventType)
                                .executeTime(entry.getHeader().getExecuteTime())
                                .build();
                        // Iterate over the row-data list and dispatch each row to the handler
                        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                            this.handlerRowData(model, rowData, entryHandler, eventType);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
                }
            } else {
                log.info("current entry type: {}", entryType);
            }
        }
    }

    /**
     * Invokes the annotation-based listener method for the given row change.
     *
     * @param model       the Canal context model
     * @param rowChange   the row change to dispatch
     * @param eventHolder the matched event holder
     * @param eventType   the Canal event type
     * @throws Exception if the listener method invocation fails
     */
    public void handlerRowData(CanalModel model, CanalEntry.RowChange rowChange, CanalEventHolder eventHolder, CanalEntry.EventType eventType) throws Exception {
        try {
            CanalContext.setModel(model);
            Method method = eventHolder.getMethod();
            ReflectionUtils.makeAccessible(method);
            Object[] args = GenericUtil.getInvokeArgs(method, model, rowChange, eventType);
            method.invoke(eventHolder.getTarget(), args);
        } finally {
            // Clear the thread-local context
            CanalContext.removeModel();
        }
    }

    /**
     * Delegates the given row data to the programmatic {@link EntryHandler} via the row-data handler.
     *
     * @param model        the Canal context model
     * @param rowData      the row data to process
     * @param entryHandler the programmatic entry handler
     * @param eventType    the Canal event type
     * @throws Exception if row-data processing fails
     */
    public void handlerRowData(CanalModel model, CanalEntry.RowData rowData, EntryHandler entryHandler, CanalEntry.EventType eventType) throws Exception {
        try {
            // Bind the Canal context to the current thread
            CanalContext.setModel(model);
            // Dispatch row data to the handler
            rowDataHandler.handlerRowData(rowData, entryHandler, eventType);
        } finally {
            // Clear the thread-local context
            CanalContext.removeModel();
        }
    }
    /** Sets the application context. */

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("{}: annotation event handler is initializing....", Thread.currentThread().getName());
        // Collect all annotation-based event handlers
        Map<String, Object> eventHandlerMap = applicationContext.getBeansWithAnnotation(BinlogEventHandler.class);
        if(CollectionUtils.isEmpty(eventHandlerMap)){
            log.info("{}: not found annotation event handler.", Thread.currentThread().getName());
            return;
        }
        // Build the list of event holders
        List<CanalEventHolder> eventHolders = new ArrayList<>();
        for (Object target : eventHandlerMap.values()) {
            // Inspect each declared method of the bean
            Method[] methods = ReflectionUtils.getDeclaredMethods(target.getClass());
            for (Method method : methods) {
                OnCanalEvent canalEvent = AnnotatedElementUtils.findMergedAnnotation(method, OnCanalEvent.class);
                if (Objects.nonNull(canalEvent)) {
                    eventHolders.add(new CanalEventHolder(target, method, canalEvent));
                }
            }
        }
        this.tableEventHolderMap = HandlerUtil.getEventHolderMap(eventHolders);
        log.info("{}: annotation event handler initialized finish.", Thread.currentThread().getName());
    }

}
