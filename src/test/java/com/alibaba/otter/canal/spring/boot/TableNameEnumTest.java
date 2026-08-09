package com.alibaba.otter.canal.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import com.alibaba.otter.canal.enums.TableNameEnum;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TableNameEnum}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class TableNameEnumTest {

    @Test
    void all_hasWildcardValues() {
        TableNameEnum all = TableNameEnum.ALL;
        assertThat(all.getDestination()).isEqualTo("*");
        assertThat(all.getSchema()).isEqualTo("*");
        assertThat(all.getTable()).isEqualTo("*");
    }

    @Test
    void toString_containsSchemaAndTable() {
        assertThat(TableNameEnum.ALL.toString()).isEqualTo("*.*");
    }

    @Test
    void delimiter() {
        assertThat(TableNameEnum.DELIMITER).isEqualTo(".");
    }
}
