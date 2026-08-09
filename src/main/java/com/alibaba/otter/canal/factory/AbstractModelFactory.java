package com.alibaba.otter.canal.factory;


import com.alibaba.otter.canal.enums.TableNameEnum;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.util.GenericUtil;
import com.alibaba.otter.canal.util.HandlerUtil;

/**
 * Base implementation of {@link IModelFactory} that resolves the target entity type from
 * the {@link EntryHandler} generic type information and delegates creation to subclasses.
 *
 * @param <T> the source data type used to build model instances
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public abstract class AbstractModelFactory<T> implements IModelFactory<T> {

    @Override
    public <R> R newInstance(EntryHandler entryHandler, T t) throws Exception {
        String canalTableName = HandlerUtil.getCanalTableNameCombination(entryHandler);
        if (TableNameEnum.ALL.name().toLowerCase().equals(canalTableName)) {
            return (R) t;
        }
        Class<R> tableClass = GenericUtil.getTableClass(entryHandler);
        if (tableClass != null) {
            return newInstance(tableClass, t);
        }
        return null;
    }

    /**
     * Creates a new instance of the given target class populated from the source data.
     *
     * @param tableClass the target entity class
     * @param t          the source data
     * @param <R>        the target entity type
     * @return a new entity instance, or {@code null} if it cannot be created
     * @throws Exception if instantiation or property population fails
     */
    abstract <R> R newInstance(Class<R> tableClass, T t) throws Exception;
}
