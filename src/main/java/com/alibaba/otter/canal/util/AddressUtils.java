package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.spring.boot.CanalSimpleProperties;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for parsing Canal server address strings into {@link InetSocketAddress} instances.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class AddressUtils {

    /**
     * Parses a comma-separated {@code host:port} address list into a list of socket addresses.
     * When the port is omitted the default Canal port ({@code 11111}) is used.
     *
     * @param addresses a comma-separated address string, e.g. {@code "127.0.0.1:11111,host2:11112"}
     * @return the parsed socket addresses
     */
    public static List<InetSocketAddress> parseAddresses(String addresses) {
        List<InetSocketAddress> parsedAddresses = new ArrayList<>();
        for (String address : StringUtils.commaDelimitedListToStringArray(addresses)) {
            if (StringUtils.hasText(address)) {
                String[] split = StringUtils.split(address, ":");
                Integer port = split.length == 1 ? CanalSimpleProperties.DEFAULT_PORT : Integer.parseInt(split[1]);
                parsedAddresses.add(new InetSocketAddress(split[0], port));
            }
        }
        return parsedAddresses;
    }

}
