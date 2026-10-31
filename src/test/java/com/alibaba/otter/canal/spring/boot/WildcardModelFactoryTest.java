package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.annotation.CanalTable;
import com.alibaba.otter.canal.factory.EntryColumnModelFactory;
import com.alibaba.otter.canal.factory.MapColumnModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WildcardModelFactoryTest {

    @CanalTable
    static class AllTables implements EntryHandler<Map<String, String>> { }

    @Test
    void mapFactoryPreservesColumnsForAllTablesHandler() throws Exception {
        Map<String, String> row = Map.of("id", "5", "title", "book");
        Map<String, String> model = new MapColumnModelFactory().newInstance(new AllTables(), row);
        assertThat(model).isEqualTo(row);
    }

    @Test
    void protobufFactoryBuildsAllAndUpdatedColumnsForAllTablesHandler() throws Exception {
        CanalEntry.Column id = CanalEntry.Column.newBuilder()
                .setName("id").setValue("5").build();
        CanalEntry.Column title = CanalEntry.Column.newBuilder()
                .setName("title").setValue("book").setUpdated(true).build();
        EntryColumnModelFactory factory = new EntryColumnModelFactory();
        Map<String, String> full = factory.newInstance(new AllTables(), List.of(id, title));
        Map<String, String> updated = factory.newInstance(new AllTables(),
                List.of(id, title), Set.of("title"));
        assertThat(full).containsEntry("id", "5").containsEntry("title", "book");
        assertThat(updated).containsOnlyKeys("title").containsEntry("title", "book");
    }
}
