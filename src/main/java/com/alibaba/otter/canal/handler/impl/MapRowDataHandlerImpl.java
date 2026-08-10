package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.factory.IModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link RowDataHandler} implementation that processes row data represented as a list of
 * column-name-to-value maps, dispatching INSERT, UPDATE and DELETE events to the entry handler.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class MapRowDataHandlerImpl implements RowDataHandler<List<Map<String, String>>> {

    private IModelFactory<Map<String,String>> modelFactory;

    /**
     * Constructs a new handler backed by the given model factory.
     *
     * @param modelFactory the factory used to build model instances from row data maps
     */
    public MapRowDataHandlerImpl(IModelFactory<Map<String, String>> modelFactory) {
        this.modelFactory = modelFactory;
    }

    @Override
    public <R> void handlerRowData(List<Map<String, String>> list, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) throws Exception{
        if (Objects.isNull(list) || Objects.isNull(entryHandler) || Objects.isNull(eventType)) {
            return;
        }
        switch (eventType) {
            case INSERT:
                R entry  = modelFactory.newInstance(entryHandler, list.get(0));
                entryHandler.insert(entry);
                break;
            case UPDATE:
                R before = modelFactory.newInstance(entryHandler, list.get(1));
                R after = modelFactory.newInstance(entryHandler, list.get(0));
                entryHandler.update(before, after);
                break;
            case DELETE:
                R o = modelFactory.newInstance(entryHandler, list.get(0));
                entryHandler.delete(o);
                break;
            default:
                break;
        }
    }
}
