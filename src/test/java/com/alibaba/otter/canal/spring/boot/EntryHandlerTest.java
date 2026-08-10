package com.alibaba.otter.canal.spring.boot;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.alibaba.otter.canal.handler.EntryHandler;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EntryHandler} default methods.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class EntryHandlerTest {

    @Test
    void defaultInsert_doesNotThrow() {
        EntryHandler<String> handler = new EntryHandler<String>() {};
        assertThatCode(() -> handler.insert("test")).doesNotThrowAnyException();
    }

    @Test
    void defaultUpdate_doesNotThrow() {
        EntryHandler<String> handler = new EntryHandler<String>() {};
        assertThatCode(() -> handler.update("before", "after")).doesNotThrowAnyException();
    }

    @Test
    void defaultDelete_doesNotThrow() {
        EntryHandler<String> handler = new EntryHandler<String>() {};
        assertThatCode(() -> handler.delete("test")).doesNotThrowAnyException();
    }
}
