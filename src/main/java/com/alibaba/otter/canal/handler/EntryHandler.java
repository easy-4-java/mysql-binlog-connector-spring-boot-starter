package com.alibaba.otter.canal.handler;

/**
 * Callback interface for handling Canal change-data entries of a given entity type.
 *
 * @param <R> the entity type produced for each change event
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface EntryHandler<R> {



    /**
     * Called when a row is inserted.
     *
     * @param t the inserted entity
     */
    default void insert(R t) {

    }


    /**
     * Called when a row is updated.
     *
     * @param before the entity state before the update
     * @param after  the entity state after the update
     */
    default void update(R before, R after) {

    }


    /**
     * Called when a row is deleted.
     *
     * @param t the deleted entity
     */
    default void delete(R t) {

    }
}
