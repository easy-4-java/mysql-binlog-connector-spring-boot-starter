package com.alibaba.otter.canal.util;


import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflection utilities for resolving generic types of {@link EntryHandler} instances and
 * building reflective invocation argument arrays.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class GenericUtil {

    private static Map<Class<? extends EntryHandler>, Class> cache = new ConcurrentHashMap<>();

    /**
     * Builds the invocation arguments for a listener method whose declared parameter types
     * may include {@link CanalModel}, {@link CanalEntry.RowChange} and {@link CanalEntry.EventType}.
     *
     * @param method    the listener method
     * @param model     the Canal model argument
     * @param rowChange the row change argument
     * @param eventType the event type argument
     * @return the resolved argument array
     */
    public static Object[] getInvokeArgs(Method method, CanalModel model, CanalEntry.RowChange rowChange, CanalEntry.EventType eventType) {
        return Arrays.stream(method.getParameterTypes()).map(pClass -> {
                    if(CanalModel.class.isAssignableFrom(pClass)){
                        return model;
                    }
                    if(CanalEntry.RowChange.class.isAssignableFrom(pClass)) {
                        return rowChange;
                    }
                    if(CanalEntry.EventType.class.isAssignableFrom(pClass)) {
                        return eventType;
                    }
                    return null;
                })
                .toArray();
    }

    /**
     * Builds the invocation arguments for a listener method whose declared parameter types
     * may include {@link CanalModel}, a row-data list and {@link CanalEntry.EventType}.
     *
     * @param method    the listener method
     * @param model     the Canal model argument
     * @param rowData   the row-data argument
     * @param eventType the event type argument
     * @return the resolved argument array
     */
    public static Object[] getInvokeArgs(Method method, CanalModel model, List<Map<String, String>> rowData, CanalEntry.EventType eventType) {
        return Arrays.stream(method.getParameterTypes()).map(pClass -> {
                if(CanalModel.class.isAssignableFrom(pClass)){
                    return model;
                }
                if(List.class.isAssignableFrom(pClass)) {
                    return rowData;
                }
                if(CanalEntry.EventType.class.isAssignableFrom(pClass)) {
                    return eventType;
                }
                return null;
            }).toArray();
    }

    /**
     * Returns the table name declared by the MyBatis-Plus table annotation metadata of the
     * {@link EntryHandler}'s generic entity type.
     *
     * @param entryHandler the entry handler
     * @return the table name, or {@code null} when not available
     */
    public static String getTableGenericProperties(EntryHandler entryHandler) {
        Class<?> tableClass = getTableClass(entryHandler);
        if (tableClass != null) {
            // Resolve MyBatis-Plus table annotation metadata
            TableInfo tableInfo = TableInfoHelper.getTableInfo(tableClass);
            if (Objects.nonNull(tableInfo)) {
                return tableInfo.getTableName();
            }
        }
        return null;
    }


    /**
     * Resolves the generic entity type declared by the given {@link EntryHandler} and caches
     * the result per handler class.
     *
     * @param object the entry handler
     * @param <T>    the entity type
     * @return the entity class, or {@code null} when it cannot be resolved
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTableClass(EntryHandler object) {
        // Resolve the handler's generic entity type
        Class<? extends EntryHandler> handlerClass = object.getClass();
        Class tableClass = cache.get(handlerClass);
        if (tableClass == null) {
            Type[] interfacesTypes = handlerClass.getGenericInterfaces();
            for (Type t : interfacesTypes) {
                Class c = (Class) ((ParameterizedType) t).getRawType();
                if (c.equals(EntryHandler.class)) {
                    tableClass = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
                    cache.putIfAbsent(handlerClass, tableClass);
                    return tableClass;
                }
            }
        }
        return tableClass;
    }


}
