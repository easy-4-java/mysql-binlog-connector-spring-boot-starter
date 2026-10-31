package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.factory.EntryColumnModelFactory;
import com.alibaba.otter.canal.factory.MapColumnModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ModelFactoryTest {

    @TableName("books")
    public static class Book {
        @TableId private String id;
        @TableField("title") private String title;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    static class BookHandler implements EntryHandler<Book> { }

    @BeforeAll
    static void initializeTableMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Book.class);
    }

    @Test
    void mapColumnsPopulateMybatisMappedProperties() throws Exception {
        Book book = new MapColumnModelFactory().newInstance(new BookHandler(),
                Map.of("title", "The Trial"));
        assertThat(book.getTitle()).isEqualTo("The Trial");
    }

    @Test
    void protobufColumnsPopulateEntityAndOnlyUpdatedFields() throws Exception {
        BookHandler handler = new BookHandler();
        EntryColumnModelFactory factory = new EntryColumnModelFactory();
        CanalEntry.Column title = CanalEntry.Column.newBuilder()
                .setName("title").setValue("New title").setUpdated(true).build();
        CanalEntry.Column id = CanalEntry.Column.newBuilder()
                .setName("id").setValue("123").setUpdated(false).build();
        Book full = factory.newInstance(handler, List.of(id, title));
        Book changed = factory.newInstance(handler, List.of(id, title), Set.of("title"));
        assertThat(full.getTitle()).isEqualTo("New title");
        assertThat(changed.getTitle()).isEqualTo("New title");
        assertThat(changed.getId()).isNull();
        Book empty = factory.newInstance(handler, List.of());
        assertThat(empty).isNull();
    }
}
