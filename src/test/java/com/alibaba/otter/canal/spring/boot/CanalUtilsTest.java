package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.util.CanalUtils;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CanalUtilsTest {

    private CanalEntry.Header header() {
        return CanalEntry.Header.newBuilder()
                .setLogfileName("mysql-bin.000001")
                .setLogfileOffset(42)
                .setExecuteTime(1000)
                .setGtid("uuid:1")
                .addProps(CanalEntry.Pair.newBuilder().setKey("curtGtid").setValue("uuid:1"))
                .addProps(CanalEntry.Pair.newBuilder().setKey("curtGtidSn").setValue("1"))
                .addProps(CanalEntry.Pair.newBuilder().setKey("curtGtidLct").setValue("0"))
                .buildPartial();
    }

    @Test
    void formatsPositionAndExtractsHeaderGtidProperties() {
        CanalEntry.Header header = header();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder().setHeader(header).buildPartial();

        assertThat(CanalUtils.buildPositionForDump(entry))
                .startsWith("mysql-bin.000001:42:1000(")
                .contains("gtid(uuid:1)");
        assertThat(CanalUtils.getCurrentGtid(header)).isEqualTo("uuid:1");
        assertThat(CanalUtils.getCurrentGtidSn(header)).isEqualTo("1");
        assertThat(CanalUtils.getCurrentGtidLct(header)).isEqualTo("0");
        CanalEntry.Header empty = CanalEntry.Header.newBuilder().buildPartial();
        assertThat(CanalUtils.getCurrentGtid(empty)).isEmpty();
        assertThat(CanalUtils.getCurrentGtidSn(empty)).isEmpty();
        assertThat(CanalUtils.getCurrentGtidLct(empty)).isEmpty();
    }

    @Test
    void unrelatedHeaderPropertiesDoNotCreateCurrentGtid() {
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .addProps(CanalEntry.Pair.newBuilder().setKey("other").setValue("value"))
                .buildPartial();
        assertThat(CanalUtils.getCurrentGtid(header)).isEmpty();
    }

    @Test
    void parsesValidRowAndTransactionEntries() {
        CanalEntry.Header header = header();
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("status").setValue("ready").setMysqlType("VARCHAR").setUpdated(true).build();
        CanalEntry.RowData row = CanalEntry.RowData.newBuilder().addAfterColumns(column).build();
        CanalEntry.RowChange change = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.INSERT).addRowDatas(row).build();
        CanalEntry.Entry rowEntry = CanalEntry.Entry.newBuilder()
                .setHeader(header).setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(change.toByteString()).buildPartial();
        CanalEntry.Entry begin = CanalEntry.Entry.newBuilder()
                .setHeader(header).setEntryType(CanalEntry.EntryType.TRANSACTIONBEGIN)
                .setStoreValue(CanalEntry.TransactionBegin.newBuilder().setThreadId(7).build().toByteString())
                .buildPartial();
        CanalEntry.Entry end = CanalEntry.Entry.newBuilder()
                .setHeader(header).setEntryType(CanalEntry.EntryType.TRANSACTIONEND)
                .setStoreValue(CanalEntry.TransactionEnd.newBuilder().setTransactionId("tx").build().toByteString())
                .buildPartial();

        assertThatCode(() -> CanalUtils.printEntry(List.of(begin, rowEntry, end))).doesNotThrowAnyException();
        assertThatCode(() -> CanalUtils.printSummary(new Message(5, List.of(rowEntry)), 5, 1))
                .doesNotThrowAnyException();
        assertThatCode(() -> CanalUtils.printSummary(new Message(0, List.of()), 0, 0))
                .doesNotThrowAnyException();
        assertThatCode(() -> CanalUtils.printColumn(List.of(column))).doesNotThrowAnyException();
        CanalUtils.printXAInfo(null);
        CanalUtils.printXAInfo(List.of(
                CanalEntry.Pair.newBuilder().setKey("XA_TYPE").setValue("PREPARE").build(),
                CanalEntry.Pair.newBuilder().setKey("XA_XID").setValue("xid-1").build()));
    }

    @Test
    void reportsMalformedRowPayload() {
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(header()).setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(ByteString.copyFromUtf8("bad protobuf")).buildPartial();

        assertThatThrownBy(() -> CanalUtils.printEntry(List.of(entry)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("parse event has an error");
    }

    @Test
    void reportsMalformedTransactionBoundaryPayloads() {
        for (CanalEntry.EntryType type : List.of(
                CanalEntry.EntryType.TRANSACTIONBEGIN,
                CanalEntry.EntryType.TRANSACTIONEND)) {
            CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                    .setHeader(header()).setEntryType(type)
                    .setStoreValue(ByteString.copyFromUtf8("not protobuf")).buildPartial();
            assertThatThrownBy(() -> CanalUtils.printEntry(List.of(entry)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("parse event has an error");
        }
    }

    @Test
    void formatsPositionWithoutGtidWhenHeaderHasNone() {
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(CanalEntry.Header.newBuilder()
                        .setLogfileName("mysql-bin.000002")
                        .setLogfileOffset(55).setExecuteTime(1000).buildPartial())
                .buildPartial();
        assertThat(CanalUtils.buildPositionForDump(entry))
                .startsWith("mysql-bin.000002:55:1000(")
                .doesNotContain("gtid(");
    }

    @Test
    void printsValidDdlAndDeletedBinaryRowWithoutParseFailure() {
        CanalEntry.Column binary = CanalEntry.Column.newBuilder()
                .setName("payload").setValue("bytes")
                .setMysqlType("BLOB").setUpdated(true).build();
        CanalEntry.RowChange ddl = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.QUERY).setIsDdl(true)
                .setSql("ALTER TABLE books ADD COLUMN title VARCHAR(20)").build();
        CanalEntry.RowChange delete = CanalEntry.RowChange.newBuilder()
                .setEventType(CanalEntry.EventType.DELETE)
                .addRowDatas(CanalEntry.RowData.newBuilder().addBeforeColumns(binary)).build();
        CanalEntry.Entry ddlEntry = CanalEntry.Entry.newBuilder()
                .setHeader(header()).setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(ddl.toByteString()).buildPartial();
        CanalEntry.Entry deleteEntry = CanalEntry.Entry.newBuilder()
                .setHeader(header()).setEntryType(CanalEntry.EntryType.ROWDATA)
                .setStoreValue(delete.toByteString()).buildPartial();

        assertThatCode(() -> CanalUtils.printEntry(List.of(ddlEntry, deleteEntry)))
                .doesNotThrowAnyException();
        assertThatCode(() -> CanalUtils.printColumn(List.of(binary)))
                .doesNotThrowAnyException();
    }
}

