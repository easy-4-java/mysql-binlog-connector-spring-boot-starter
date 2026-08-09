package com.alibaba.otter.canal.factory;


import com.alibaba.otter.canal.handler.EntryHandler;

import java.util.Set;

/**
 * Factory contract for building model instances from raw Canal change data.
 *
 * @param <T> the source data type used to build model instances
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface IModelFactory<T> {


    /**
     * Creates a new model instance of the handler's entity type from the given source data.
     *
     * @param entryHandler the handler declaring the target entity type
     * @param t            the source data
     * @param <R>          the target entity type
     * @return a new model instance, or {@code null} if it cannot be created
     * @throws Exception if instantiation fails
     */
    <R> R newInstance(EntryHandler entryHandler, T t) throws Exception;

    /**
     * Creates a new model instance restricted to the given updated columns.
     *
     * @param entryHandler the handler declaring the target entity type
     * @param t            the source data
     * @param updateColumn the set of column names that were updated
     * @param <R>          the target entity type
     * @return a new model instance, or {@code null} by default
     * @throws Exception if instantiation fails
     */
    default <R> R newInstance(EntryHandler entryHandler, T t, Set<String> updateColumn) throws Exception {
        return null;
    }
}
