package com.alibaba.otter.canal.util;


import com.alibaba.otter.canal.annotation.CanalEventHolder;
import com.alibaba.otter.canal.annotation.CanalTable;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.enums.TableNameEnum;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utilities for resolving Canal {@link EntryHandler} and {@link CanalEventHolder} instances
 * based on destination, schema, table and event type, and for building the lookup keys used
 * by the message handlers.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class HandlerUtil {

    protected static Map<String, Predicate<CanalEventHolder>> eventPredicateMap = new ConcurrentHashMap<>();

    /**
     * Finds the entry handler matching the given schema and table from a list of handlers.
     * Falls back to a global (all-tables) handler when no exact match exists.
     *
     * @param entryHandlers the available entry handlers
     * @param schemaName    the database schema name
     * @param tableName     the table name
     * @return the matching entry handler, or the global handler, or {@code null}
     */
    public static EntryHandler getEntryHandler(List<? extends EntryHandler> entryHandlers, String schemaName, String tableName) {
        StringJoiner joiner = new StringJoiner(".").add(schemaName).add(tableName);
        EntryHandler globalHandler = null;
        for (EntryHandler handler : entryHandlers) {
            String canalTableNameCombination = getCanalTableNameCombination(handler);
            if (StringUtils.isBlank(canalTableNameCombination)) {
                continue;
            }
            if (TableNameEnum.ALL.name().toLowerCase().equals(canalTableNameCombination)) {
                globalHandler = handler;
                continue;
            }
            if (canalTableNameCombination.equals(joiner.toString().toLowerCase())) {
                return handler;
            }
            String name = GenericUtil.getTableGenericProperties(handler);
            if (name != null) {
                if (name.equals(tableName)) {
                    return handler;
                }
            }
        }
        return globalHandler;
    }


    /**
     * Builds a lookup map of entry handlers keyed by their destination/schema/table combination
     * (or by the MyBatis-Plus table name when no combination is available).
     *
     * @param entryHandlers the available entry handlers
     * @return a map from lookup key to the first matching entry handler
     */
    public static Map<String, EntryHandler> getTableHandlerMap(List<? extends EntryHandler> entryHandlers) {
        Map<String, EntryHandler> map = new ConcurrentHashMap<>();
        if (CollectionUtils.isEmpty(entryHandlers)) {
            return map;
        }
        for (EntryHandler handler : entryHandlers) {
            String canalTableNameCombination = getCanalTableNameCombination(handler);
            if (StringUtils.isNotBlank(canalTableNameCombination)) {
                map.putIfAbsent(canalTableNameCombination.toLowerCase(), handler);
            } else {
                String name = GenericUtil.getTableGenericProperties(handler);
                if (name != null) {
                    map.putIfAbsent(name.toLowerCase(), handler);
                }
            }
        }
        return map;
    }

    /**
     * Builds a lookup map of event holders grouped by the concatenation of destination, schema,
     * table and event type.
     *
     * @param eventHolders the available event holders
     * @return a map from lookup key to the list of matching event holders
     */
    public static Map<String, List<CanalEventHolder>> getEventHolderMap(List<CanalEventHolder> eventHolders) {
        Map<String, List<CanalEventHolder>> map = new ConcurrentHashMap<>();
        if (CollectionUtils.isEmpty(eventHolders)) {
            return map;
        }
        for (CanalEventHolder holder : eventHolders) {
            List<String> canalTableNameCombinations = getCanalTableNameCombinations(holder);
            if (CollectionUtils.isEmpty(canalTableNameCombinations)) {
                continue;
            }
            for (String canalTableNameCombination : canalTableNameCombinations) {
                map.computeIfAbsent(canalTableNameCombination, k -> new ArrayList<>()).add(holder);
            }
        }
        return map;
    }

    /**
     * Returns the event holders matching the given destination, schema, table and event type
     * from the provided lookup map.
     *
     * @param map          the event holder lookup map
     * @param destination  the Canal destination
     * @param schemaName   the database schema name
     * @param tableName    the table name
     * @param eventType    the Canal event type
     * @return the list of matching event holders
     */
    public static List<CanalEventHolder> getEventHolders(Map<String, List<CanalEventHolder>> map,
                                                        String destination,
                                                        String schemaName,
                                                        String tableName,
                                                        CanalEntry.EventType eventType) {
        // Build the lookup key from the four attributes
        String key = getCombinationValue(destination, schemaName, tableName, eventType);
        // Resolve the predicate for the lookup key
        Predicate<CanalEventHolder> predicate =  eventPredicateMap.computeIfAbsent(key, k -> getAnnotationFilter(destination, schemaName, tableName, eventType));
        // Return the filtered results
        return map.getOrDefault(key, Collections.emptyList()).stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Returns the entry handler matching the given schema and table from a lookup map,
     * falling back to the global (all-tables) handler when no exact match exists.
     *
     * @param map        the entry handler lookup map
     * @param schemaName the database schema name
     * @param tableName  the table name
     * @return the matching entry handler, or the global handler, or {@code null}
     */
    public static EntryHandler getEntryHandler(Map<String, EntryHandler> map, String schemaName, String tableName) {
        StringJoiner joiner = new StringJoiner(".").add(schemaName).add(tableName);
        EntryHandler entryHandler = map.get(joiner.toString().toLowerCase());
        if (entryHandler == null) {
            return map.get(TableNameEnum.ALL.name().toLowerCase());
        }
        return entryHandler;
    }

    /**
     * Builds a predicate that matches event holders whose destination, schema, table and
     * event type annotations are compatible with the given values.
     *
     * @param destination the Canal destination
     * @param schemaName  the database schema name
     * @param tableName   the table name
     * @param eventType   the Canal event type
     * @return the composed matching predicate
     */
    protected static Predicate<CanalEventHolder> getAnnotationFilter(String destination,
                                                                     String schemaName,
                                                                     String tableName,
                                                                     CanalEntry.EventType eventType) {

        // Match destination; when not specified the holder matches all destinations
        Predicate<CanalEventHolder> df = holder -> StringUtils.isEmpty(holder.getEvent().destination())
                || holder.getEvent().destination().equals(destination) || destination == null;

        // Match database schema name
        Predicate<CanalEventHolder> sf = holder -> StringUtils.isNotBlank(holder.getEvent().schema())
                && holder.getEvent().schema().equalsIgnoreCase(schemaName);

        // Match table name; when not specified the holder matches all tables
        Predicate<CanalEventHolder> tf = holder -> StringUtils.isNotBlank(holder.getEvent().table())
                && ( holder.getEvent().table().equalsIgnoreCase(tableName) || holder.getEvent().table().equals(TableNameEnum.ALL.getTable()) );

        // Match event type
        Predicate<CanalEventHolder> ef = holder -> holder.getEvent().eventType().length > 0 && Arrays.stream(holder.getEvent().eventType()).anyMatch(ev -> ev == eventType) ;

        return df.and(sf).and(tf).and(ef);
    }

    /**
     * Returns the destination/schema/table combination key declared by the given entry
     * handler's {@link CanalTable} annotation, or {@code null} when not annotated.
     *
     * @param entryHandler the entry handler
     * @return the combination key, or {@code null}
     */
    public static String getCanalTableNameCombination(EntryHandler entryHandler) {
        CanalTable canalTable = entryHandler.getClass().getAnnotation(CanalTable.class);
        if (Objects.nonNull(canalTable)) {
            return getCombinationValue(canalTable.destination(), canalTable.schema(), canalTable.table());
        }
        return null;
    }

    /**
     * Returns the destination/schema/table/event-type combination keys declared by the given
     * event holder's {@link OnCanalEvent} annotation, one per subscribed event type.
     *
     * @param eventHolder the event holder
     * @return the list of combination keys, or {@code null} when no event types are declared
     */
    public static List<String> getCanalTableNameCombinations(CanalEventHolder eventHolder) {
        OnCanalEvent canalEvent = eventHolder.getEvent();
        if (Objects.nonNull(canalEvent) && Objects.nonNull(canalEvent.eventType()) && canalEvent.eventType().length > 0) {
            return Arrays.stream(canalEvent.eventType())
                    .map(eventType -> getCombinationValue(canalEvent.destination(), canalEvent.schema(), canalEvent.table(), eventType))
                    .distinct().collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Builds a lower-cased destination/schema/table combination key, substituting wildcards
     * ({@link TableNameEnum#ALL}) for any blank segment.
     *
     * @param destination the destination segment
     * @param schema      the schema segment
     * @param table       the table segment
     * @return the lower-cased combination key
     */
    public static String getCombinationValue(String destination, String schema, String table) {
        destination = StringUtils.defaultIfBlank(destination, TableNameEnum.ALL.getDestination());
        schema = StringUtils.defaultIfBlank(schema, TableNameEnum.ALL.getSchema());
        table = StringUtils.defaultIfBlank(table, TableNameEnum.ALL.getTable());
        StringJoiner joiner = new StringJoiner(TableNameEnum.DELIMITER).add(destination).add(schema).add(table);
        return joiner.toString().toLowerCase();
    }

    /**
     * Builds a lower-cased destination/schema/table/event-type combination key, substituting
     * wildcards ({@link TableNameEnum#ALL}) for any blank segment.
     *
     * @param destination the destination segment
     * @param schema      the schema segment
     * @param table       the table segment
     * @param eventType   the event type segment
     * @return the lower-cased combination key
     */
    public static String getCombinationValue(String destination, String schema, String table, CanalEntry.EventType eventType) {
        destination = StringUtils.defaultIfBlank(destination, TableNameEnum.ALL.getDestination());
        schema = StringUtils.defaultIfBlank(schema, TableNameEnum.ALL.getSchema());
        table = StringUtils.defaultIfBlank(table, TableNameEnum.ALL.getTable());
        StringJoiner joiner = new StringJoiner(TableNameEnum.DELIMITER).add(destination).add(schema).add(table).add(eventType.name().toLowerCase());
        return joiner.toString().toLowerCase();
    }

}
