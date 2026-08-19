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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

@ConfigurationProperties(CanalThreadPoolProperties.PREFIX)
@Data
public class CanalThreadPoolProperties {

	public static final String PREFIX = "canal.thread-pool";

	private int corePoolSize = 1;

	private int maxPoolSize = Runtime.getRuntime().availableProcessors();

	private int queueCapacity = Integer.MAX_VALUE;

	private Duration keepAlive = Duration.ofSeconds(60);

	private boolean allowCoreThreadTimeOut = false;

	private boolean waitForTasksToCompleteOnShutdown = false;

	private int awaitTerminationSeconds = 0;

	private String threadNamePrefix = "RedisAsyncTaskExecutor-";

	private boolean daemon = false;

	private RejectedPolicy rejectedPolicy = RejectedPolicy.AbortPolicy;


	/**
	 * Rejected-execution policies.
	 * CallerRunsPolicy()  - run the task on the caller thread (e.g. main thread)
	 * AbortPolicy()        - throw a RejectedExecutionException
	 * DiscardPolicy()      - silently discard the task
	 * DiscardOldestPolicy()- discard the oldest queued task
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
	 */
	public enum RejectedPolicy {

		AbortPolicy((e) -> {
			return new ThreadPoolExecutor.AbortPolicy();
		}),
		CallerRunsPolicy((e) -> {
			return new ThreadPoolExecutor.CallerRunsPolicy();
		}),
		DiscardPolicy((e) -> {
			return new ThreadPoolExecutor.DiscardPolicy();
		}),
		DiscardOldestPolicy((e) -> {
			return new ThreadPoolExecutor.DiscardOldestPolicy();
		});

		private Function<Object, RejectedExecutionHandler> function;

		private RejectedPolicy(Function<Object, RejectedExecutionHandler> function) {
			this.function = function;
		}
		/** Gets the rejected execution handler. */

		public RejectedExecutionHandler getRejectedExecutionHandler(){
			return this.function.apply(null);
		}

	}
}
