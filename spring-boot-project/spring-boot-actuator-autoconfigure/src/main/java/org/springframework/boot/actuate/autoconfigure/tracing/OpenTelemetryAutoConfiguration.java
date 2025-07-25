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

package org.springframework.boot.actuate.autoconfigure.tracing;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for OpenTelemetry tracing.
 *
 * @author Moritz Halbritter
 * @author Marcin Grzejszczak
 * @author Yanming Zhou
 * @since 3.0.0
 * @deprecated since 3.4.0 in favor of {@link OpenTelemetryTracingAutoConfiguration}
 */
@Deprecated(since = "3.4.0", forRemoval = true)
public class OpenTelemetryAutoConfiguration {

}
