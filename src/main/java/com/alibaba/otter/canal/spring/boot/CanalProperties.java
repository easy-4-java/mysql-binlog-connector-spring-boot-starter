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

@ConfigurationProperties(CanalProperties.PREFIX)
@Data
public class CanalProperties {

	public static final String PREFIX = "canal";

	public static final String CANAL_ASYNC = PREFIX + "." + "async";
	public static final String CANAL_MODE = PREFIX + "." + "mode";
	public static final String CANAL_INSTANCES = PREFIX + "." + "instances";

	private ClientMode mode = ClientMode.simple;
	private Boolean async;
	private String filter = StringUtils.EMPTY;
	private Integer batchSize = 1000;
	private Long timeout = -1L;
	private TimeUnit unit = TimeUnit.SECONDS;
	private List<CanalEntry.EntryType> subscribeTypes = Arrays.asList(CanalEntry.EntryType.ROWDATA);

	/**
	 * Canal server modes. One of simple, cluster, kafka, pulsarmq, rabbitmq, rocketmq.
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
	 */
	public enum ClientMode {
		simple, cluster, kafka, pulsarmq, rabbitmq, rocketmq
	}

}
