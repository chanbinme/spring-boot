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

package org.springframework.boot.test.autoconfigure.data.mongo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.env.Environment;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Annotation that can be used for a MongoDB test that focuses <strong>only</strong> on
 * MongoDB components.
 * <p>
 * Using this annotation only enables auto-configuration that is relevant to Data Mongo
 * tests. Similarly, component scanning is limited to Mongo repositories and entities
 * ({@code @Document}).
 * <p>
 * When using JUnit 4, this annotation should be used in combination with
 * {@code @RunWith(SpringRunner.class)}.
 *
 * @author Michael Simons
 * @author Stephane Nicoll
 * @author Artsiom Yudovin
 * @since 1.5.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(DataMongoTestContextBootstrapper.class)
@ExtendWith(SpringExtension.class)
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(DataMongoTypeExcludeFilter.class)
@AutoConfigureCache
@AutoConfigureDataMongo
@ImportAutoConfiguration
public @interface DataMongoTest {

	/**
	 * Properties in form {@literal key=value} that should be added to the Spring
	 * {@link Environment} before the test runs.
	 * @return the properties to add
	 * @since 2.1.0
	 */
	String[] properties() default {};

	/**
	 * Determines if default filtering should be used with
	 * {@link SpringBootApplication @SpringBootApplication}. By default no beans are
	 * included.
	 * @see #includeFilters()
	 * @see #excludeFilters()
	 * @return if default filters should be used
	 */
	boolean useDefaultFilters() default true;

	/**
	 * A set of include filters which can be used to add otherwise filtered beans to the
	 * application context.
	 * @return include filters to apply
	 */
	Filter[] includeFilters() default {};

	/**
	 * A set of exclude filters which can be used to filter beans that would otherwise be
	 * added to the application context.
	 * @return exclude filters to apply
	 */
	Filter[] excludeFilters() default {};

	/**
	 * Auto-configuration exclusions that should be applied for this test.
	 * @return auto-configuration exclusions to apply
	 */
	@AliasFor(annotation = ImportAutoConfiguration.class, attribute = "exclude")
	Class<?>[] excludeAutoConfiguration() default {};

}
