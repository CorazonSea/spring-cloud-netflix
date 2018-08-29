/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.netflix.cconcurrency.limits.reactive;

import java.util.function.Consumer;

import com.netflix.concurrency.limits.limit.SettableLimit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.cconcurrency.limits.test.AbstractConcurrencyLimitsTests;
import org.springframework.cloud.test.ClassPathExclusions;
import org.springframework.cloud.test.ModifiedClassPathRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.SocketUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RunWith(ModifiedClassPathRunner.class)
@ClassPathExclusions({"spring-boot-starter-tomcat-*", "tomcat-embed-*"})
public class ConcurrencyLimitsWebFilterTests extends AbstractConcurrencyLimitsTests {

	public int port;

	private WebClient client;

	@Before
	public void init() {
		port = SocketUtils.findAvailableTcpPort();
		client = WebClient.create("http://localhost:"+port);
	}

	@Test
	@SuppressWarnings("Duplicates")
	public void webFilterWorks() {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder()
				.properties("server.port="+port, "spring.main.web-application-type=reactive")
				.sources(TestConfig.class).run()) {

			assertLimiter(client);
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@RestController
	protected static class TestConfig {

		@GetMapping
		public String get() throws Exception {
			return "Hello";
		}

		@Bean
		public Consumer<ServerWebExchangeLimiterBuilder> limiterBuilderConfigurer() {
			return limiterBuilder -> limiterBuilder
					.limiter(builder -> builder.limit(SettableLimit.startingAt(1)));
		}
	}

}
