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

package org.springframework.boot.autoconfigure.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional @Conditional} that matches when the specified cloud platform is
 * active.
 *
 * @author Madhura Bhave
 * @since 1.5.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnCloudPlatformCondition.class)
public @interface ConditionalOnCloudPlatform {

	/**
	 * The {@link CloudPlatform cloud platform} that must be active.
	 * @return the expected cloud platform
	 */
	CloudPlatform value();

}
