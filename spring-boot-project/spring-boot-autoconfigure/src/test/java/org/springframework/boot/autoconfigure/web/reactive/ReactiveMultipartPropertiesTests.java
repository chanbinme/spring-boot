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

package org.springframework.boot.autoconfigure.web.reactive;

import org.junit.jupiter.api.Test;

import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ReactiveMultipartProperties}
 *
 * @author Chris Bono
 */
class ReactiveMultipartPropertiesTests {

	@Test
	void defaultValuesAreConsistent() {
		ReactiveMultipartProperties multipartProperties = new ReactiveMultipartProperties();
		DefaultPartHttpMessageReader defaultPartHttpMessageReader = new DefaultPartHttpMessageReader();
		assertThat(defaultPartHttpMessageReader).hasFieldOrPropertyWithValue("maxInMemorySize",
				(int) multipartProperties.getMaxInMemorySize().toBytes());
		assertThat(defaultPartHttpMessageReader).hasFieldOrPropertyWithValue("maxHeadersSize",
				(int) multipartProperties.getMaxHeadersSize().toBytes());
		assertThat(defaultPartHttpMessageReader).hasFieldOrPropertyWithValue("maxDiskUsagePerPart",
				multipartProperties.getMaxDiskUsagePerPart().toBytes());
		assertThat(defaultPartHttpMessageReader).hasFieldOrPropertyWithValue("maxParts",
				multipartProperties.getMaxParts());
	}

}
