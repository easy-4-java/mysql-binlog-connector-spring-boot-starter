package com.alibaba.otter.canal.handler;

import com.alibaba.otter.canal.annotation.BinlogEventHandler;
import com.alibaba.otter.canal.annotation.CanalEventHolder;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract base class for Canal {@link FlatMessage} handlers. It resolves the matching
 * annotation-based event holders or {@link EntryHandler} instances for each change event
 * and delegates row-data processing to a {@link RowDataHandler}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractFlatMessageHandler implements MessageHandler<FlatMessage>, ApplicationContextAware {

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
    private RowDataHandler<List<Map<String, String>>> rowDataHandler;

    /**
     * Constructs a new flat-message handler.
     *
     * @param subscribeTypes the entry types to subscribe to, or {@code null} to use the default
     * @param entryHandlers  the programmatic entry handlers
     * @param rowDataHandler the row-data handler
     */
    public AbstractFlatMessageHandler(List<CanalEntry.EntryType> subscribeTypes,
                                      List<? extends EntryHandler> entryHandlers,
                                      RowDataHandler<List<Map<String, String>>> rowDataHandler) {
        if(Objects.nonNull(subscribeTypes)){
            this.subscribeTypes = subscribeTypes;
        }
        this.tableHandlerMap = HandlerUtil.getTableHandlerMap(entryHandlers);
        this.rowDataHandler = rowDataHandler;
    }

    @Override
    public void handleMessage(String destination, FlatMessage flatMessage) {
        // Check whether the message carries any data
        List<Map<String, String>> data = flatMessage.getData();
        if(CollectionUtils.isEmpty(data)){
            return;
        }
        // Iterate over the data, parsing one row at a time
        for (int i = 0; i < data.size(); i++) {
            // Database instance (schema) name
            String schemaName = flatMessage.getDatabase();
            // Table name
            String tableName = flatMessage.getTable();
            // Event type
            CanalEntry.EventType eventType = CanalEntry.EventType.valueOf(flatMessage.getType());
            // Current row data
            List<Map<String, String>> maps;
            if (eventType.equals(CanalEntry.EventType.UPDATE)) {
                // Data after the update
                Map<String, String> map = data.get(i);
                // Data before the update
                Map<String, String> oldMap = flatMessage.getOld().get(i);
                // Merge the new and old data
                maps = Stream.of(map, oldMap).collect(Collectors.toList());
            } else {
                maps = Stream.of(data.get(i)).collect(Collectors.toList());
            }
            try {
                // Resolve the annotation-based event holders for the table
                List<CanalEventHolder> eventHolders = HandlerUtil.getEventHolders(tableEventHolderMap, destination, schemaName, tableName, eventType);
                if(!CollectionUtils.isEmpty(eventHolders)){
                    CanalModel model = CanalModel.builder()
                            .id(flatMessage.getId())
                            .schema(schemaName)
                            .table(tableName)
                            .eventType(eventType)
                            .executeTime(flatMessage.getEs())
                            .createTime(flatMessage.getTs()).build();
                    for (CanalEventHolder eventHolder : eventHolders) {
                        this.handlerRowData(model, maps, eventHolder, eventType);
                    }
                    continue;
                }
                // Resolve the programmatic entry handler for the table
                EntryHandler<?> entryHandler = HandlerUtil.getEntryHandler(tableHandlerMap, schemaName, tableName);
                // Dispatch when a matching handler exists
                if(Objects.nonNull(entryHandler)){
                    CanalModel model = CanalModel.builder()
                            .id(flatMessage.getId())
                            .schema(schemaName)
                            .table(tableName)
                            .eventType(eventType)
                            .executeTime(flatMessage.getEs())
                            .createTime(flatMessage.getTs()).build();
                   this.handlerRowData(model, maps, entryHandler, eventType);
                }
            } catch (Exception e) {
                throw new RuntimeException("parse event has an error , data:" + maps.toString(), e);
            }
        }
    }

    /**
     * Invokes the annotation-based listener method for the given row data.
     *
     * @param model       the Canal context model
     * @param rowData     the row data to dispatch
     * @param eventHolder the matched event holder
     * @param eventType   the Canal event type
     * @throws Exception if the listener method invocation fails
     */
    public void handlerRowData(CanalModel model, List<Map<String, String>> rowData, CanalEventHolder eventHolder, CanalEntry.EventType eventType) throws Exception {
        Method method = eventHolder.getMethod();
        try {
            CanalContext.setModel(model);
            ReflectionUtils.makeAccessible(method);
            Object[] args = GenericUtil.getInvokeArgs(method, model, rowData, eventType);
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
    public void handlerRowData(CanalModel model, List<Map<String, String>> rowData, EntryHandler entryHandler, CanalEntry.EventType eventType) throws Exception {
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
