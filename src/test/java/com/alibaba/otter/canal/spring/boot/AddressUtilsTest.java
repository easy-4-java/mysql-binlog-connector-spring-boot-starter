package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.util.AddressUtils;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressUtilsTest {

    @Test
    void parsesMultipleAddressesAndDefaultPort() {
        assertThat(AddressUtils.parseAddresses("127.0.0.1:11112,127.0.0.2"))
                .extracting(InetSocketAddress::getPort)
                .containsExactly(11112, CanalSimpleProperties.DEFAULT_PORT);
        assertThat(AddressUtils.parseAddresses("127.0.0.1:11112,127.0.0.2"))
                .extracting(InetSocketAddress::getHostString)
                .containsExactly("127.0.0.1", "127.0.0.2");
    }

    @Test
    void ignoresEmptyEntriesAndRejectsNonNumericPort() {
        assertThat(AddressUtils.parseAddresses("127.0.0.1:11111,,"))
                .hasSize(1);
        assertThatThrownBy(() -> AddressUtils.parseAddresses("127.0.0.1:not-a-port"))
                .isInstanceOf(NumberFormatException.class);
    }
}
