package com.alibaba.otter.canal.spring.boot;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the Canal simple-mode client, bound to the {@code canal.simple} prefix.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@ConfigurationProperties(CanalSimpleProperties.PREFIX)
@Getter
@Setter
@ToString
public class CanalSimpleProperties {

    public static final int DEFAULT_PORT = 11111;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_MAX_SLEEP_MS = Integer.MAX_VALUE;
    public static final String PREFIX = "canal.simple";

    /**
     * The simple connector instances to create.
     */
    private List<CanalSimpleProperties.Instance> instances = new ArrayList<>();

    /**
     * Configuration for a single Canal simple connector instance.
     */
    @Data
    public static class Instance {

        /**
         * The Canal Server host address.
         */
        private String host;
        /**
         * The Canal Server port. Defaults to {@code 11111}.
         */
        private Integer port = DEFAULT_PORT;
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
