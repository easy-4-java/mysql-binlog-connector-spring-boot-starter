package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.util.RowDataUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RowDataUtilTest {

    @Test
    void extractsCaseInsensitiveBeforeAndAfterValues() {
        CanalEntry.RowData row = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(CanalEntry.Column.newBuilder().setName("ID").setValue("old"))
                .addAfterColumns(CanalEntry.Column.newBuilder().setName("id").setValue("new"))
                .build();

        assertThat(RowDataUtil.getBeforeValue(row, "id")).isEqualTo("old");
        assertThat(RowDataUtil.getAfterValue(row, "ID")).isEqualTo("new");
        assertThat(RowDataUtil.getValue(row, "Id")).isEqualTo("old");
        assertThat(RowDataUtil.getValue(row, "absent")).isNull();
    }

    @Test
    void fallsBackToAfterValueAndHandlesMissingRow() {
        CanalEntry.RowData row = CanalEntry.RowData.newBuilder()
                .addAfterColumns(CanalEntry.Column.newBuilder().setName("status").setValue("ready"))
                .build();

        assertThat(RowDataUtil.getValue(row, "STATUS")).isEqualTo("ready");
        assertThat(RowDataUtil.getBeforeValue(null, "status")).isNull();
        assertThat(RowDataUtil.getAfterValue(null, "status")).isNull();
    }
}
