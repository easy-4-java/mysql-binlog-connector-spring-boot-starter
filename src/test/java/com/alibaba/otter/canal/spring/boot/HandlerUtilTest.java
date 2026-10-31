package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.annotation.CanalEventHolder;
import com.alibaba.otter.canal.annotation.CanalTable;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.util.HandlerUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HandlerUtilTest {

    @OnCanalEvent(destination = "orders", schema = "app", table = "books",
            eventType = {CanalEntry.EventType.INSERT})
    static class InsertListener {
        public void onInsert() { }
    }

    @Test
    void routesOnlyMatchingAnnotatedEventHolder() throws Exception {
        InsertListener listener = new InsertListener();
        Method method = InsertListener.class.getMethod("onInsert");
        CanalEventHolder holder = new CanalEventHolder(listener, method,
                InsertListener.class.getAnnotation(OnCanalEvent.class));

        Map<String, List<CanalEventHolder>> map = HandlerUtil.getEventHolderMap(List.of(holder));
        assertThat(HandlerUtil.getEventHolders(map, "orders", "app", "books",
                CanalEntry.EventType.INSERT)).containsExactly(holder);
        assertThat(HandlerUtil.getEventHolders(map, "orders", "app", "books",
                CanalEntry.EventType.UPDATE)).isEmpty();
        assertThat(holder.isMatch(CanalEntry.EventType.INSERT)).isTrue();
        assertThat(holder.isMatch(CanalEntry.EventType.DELETE)).isFalse();
        assertThat(holder.getTarget()).isSameAs(listener);
        assertThat(holder.getMethod()).isSameAs(method);
    }

    @Test
    void wildcardSegmentsAreNormalizedForLookup() {
        assertThat(HandlerUtil.getCombinationValue("", "", "")).isEqualTo("*.*.*");
        assertThat(HandlerUtil.getCombinationValue("ORDERS", "APP", "BOOKS",
                CanalEntry.EventType.INSERT)).isEqualTo("orders.app.books.insert");
        assertThat(HandlerUtil.getEventHolderMap(List.of())).isEmpty();
    }

    @CanalTable(schema = "app", table = "books")
    static class WildcardBookHandler implements EntryHandler<String> { }

    @CanalTable(destination = "orders", schema = "app", table = "books")
    static class OrdersBookHandler implements EntryHandler<String> { }

    @CanalTable
    static class AllTablesHandler implements EntryHandler<String> { }

    @Test
    void destinationSpecificHandlerWinsThenFallsBackToWildcard() {
        WildcardBookHandler wildcard = new WildcardBookHandler();
        OrdersBookHandler orders = new OrdersBookHandler();
        Map<String, EntryHandler> handlers = HandlerUtil.getTableHandlerMap(List.of(wildcard, orders));
        assertThat(HandlerUtil.getEntryHandler(handlers, "orders", "app", "books"))
                .isSameAs(orders);
        assertThat(HandlerUtil.getEntryHandler(handlers, "other", "app", "books"))
                .isSameAs(wildcard);
        assertThat(HandlerUtil.getEntryHandler(handlers, "app", "books"))
                .isSameAs(wildcard);
        assertThat(HandlerUtil.getEntryHandler(handlers, "other", "app", "missing"))
                .isNull();
    }

    @Test
    void directListLookupMatchesSchemaTableThenGlobalFallback() {
        WildcardBookHandler books = new WildcardBookHandler();
        AllTablesHandler all = new AllTablesHandler();
        assertThat(HandlerUtil.getEntryHandler(List.of(all, books), "app", "books"))
                .isSameAs(books);
        assertThat(HandlerUtil.getEntryHandler(List.of(all, books), "app", "other"))
                .isSameAs(all);
    }
}

