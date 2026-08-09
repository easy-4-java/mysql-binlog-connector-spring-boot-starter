/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.alibaba.otter.canal.spring.boot;

import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Core configuration properties for the Canal client, bound to the {@code canal} prefix.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@ConfigurationProperties(CanalProperties.PREFIX)
@Data
public class CanalProperties {

	public static final String PREFIX = "canal";

	public static final String CANAL_ASYNC = PREFIX + "." + "async";
	public static final String CANAL_MODE = PREFIX + "." + "mode";
	public static final String CANAL_INSTANCES = PREFIX + "." + "instances";

	/**
	 * The Canal client mode. One of simple, cluster, kafka, rocketMQ.
	 */
	private ClientMode mode = ClientMode.simple;
	/**
	 * Whether message handling should be asynchronous. Defaults to {@code null} (treated as enabled).
	 */
	private Boolean async;
	/**
	 * The client subscription filter; the corresponding filter is updated on repeated subscriptions.
	 * <pre>
	 * Notes:
	 * a. When the filter is empty, the Canal server-side filter is used.
	 * b. When the filter is non-empty, it replaces the Canal server-side filter.
	 * </pre>
	 */
	private String filter = StringUtils.EMPTY;
	/**
	 * The number of messages read from the Canal service per batch.
	 */
	private Integer batchSize = 1000;
	/**
	 * Read timeout in the configured time unit. {@code -1} disables timeout control.
	 */
	private Long timeout = -1L;
	/**
	 * The time unit of {@link #timeout}.
	 */
	private TimeUnit unit = TimeUnit.SECONDS;
	/**
	 * Subscribed entry types, mainly used to flag transaction begin, row-data change and transaction end.
	 */
	private List<CanalEntry.EntryType> subscribeTypes = Arrays.asList(CanalEntry.EntryType.ROWDATA);

	/**
	 * Canal server modes. One of simple, cluster, kafka, pulsarmq, rabbitmq, rocketmq.
	 */
	public enum ClientMode {
		simple, cluster, kafka, pulsarmq, rabbitmq, rocketmq
	}

}
