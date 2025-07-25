/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.metrics.export.elastic;

import io.micrometer.elastic.ElasticConfig;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryPropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ElasticProperties}.
 *
 * @author Andy Wilkinson
 */
class ElasticPropertiesTests extends StepRegistryPropertiesTests {

	@Test
	void defaultValuesAreConsistent() {
		ElasticProperties properties = new ElasticProperties();
		ElasticConfig config = ElasticConfig.DEFAULT;
		assertStepRegistryDefaultValues(properties, config);
		assertThat(properties.getHost()).isEqualTo(config.host());
		assertThat(properties.getIndex()).isEqualTo(config.index());
		assertThat(properties.getIndexDateFormat()).isEqualTo(config.indexDateFormat());
		assertThat(properties.getIndexDateSeparator()).isEqualTo(config.indexDateSeparator());
		assertThat(properties.getPassword()).isEqualTo(config.password());
		assertThat(properties.getTimestampFieldName()).isEqualTo(config.timestampFieldName());
		assertThat(properties.getUserName()).isEqualTo(config.userName());
		assertThat(properties.isAutoCreateIndex()).isEqualTo(config.autoCreateIndex());
		assertThat(properties.getPipeline()).isEqualTo(config.pipeline());
		assertThat(properties.getApiKeyCredentials()).isEqualTo(config.apiKeyCredentials());
	}

}
