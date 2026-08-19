package com.alibaba.otter.canal.spring.boot;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(CanalClusterProperties.PREFIX)
@Getter
@Setter
@ToString
public class CanalClusterProperties {

    public static final int DEFAULT_PORT = 11111;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_MAX_SLEEP_MS = Integer.MAX_VALUE;
    public static final String PREFIX = "canal.cluster";

    private List<CanalClusterProperties.Instance> instances = new ArrayList<>();

    /**
     * Configuration for a single Canal cluster connector instance.
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
     */
    @Data
    public static class Instance {

        /**
         * Comma-separated Canal Server addresses, used when {@code zkServers} is not set.
         */
        private String addresses;
        /**
         * Canal ZooKeeper address. When set, the {@code addresses} property is ignored.
         */
        private String zkServers;
        /**
         * The Canal destination (canal instance name).
         */
        private String destination;
        /**
         * The Canal Server account username.
         */
        private String username;
        /**
         * The Canal Server account password.
         */
        private String password;
        /**
         * Socket connect timeout in milliseconds. Defaults to {@code 60000}.
         */
        private int soTimeout     = 60000;
        /**
         * Socket idle timeout in milliseconds. Defaults to {@code 3600000} (one hour).
         */
        private int idleTimeout   = 60 * 60 * 1000;
        /**
         * Number of retry attempts. Set to {@code -1} to block on subscribe for a graceful shutdown.
         */
        private int retryTimes    = 3;
        /**
         * Retry interval in milliseconds. Defaults to {@code 5000} (5 seconds).
         */
        private int retryInterval = 5000;

    }

}
