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

package org.springframework.boot.autoconfigure.ldap;

import javax.naming.Name;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.pool2.factory.PoolConfig;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link LdapAutoConfiguration}.
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Vedran Pavic
 */
class LdapAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(LdapAutoConfiguration.class));

	@Test
	void contextSourceWithDefaultUrl() {
		this.contextRunner.run((context) -> {
			LdapContextSource contextSource = context.getBean(LdapContextSource.class);
			assertThat(contextSource.getUrls()).containsExactly("ldap://localhost:389");
			assertThat(contextSource.isAnonymousReadOnly()).isTrue();
		});
	}

	@Test
	void contextSourceWithSingleUrl() {
		this.contextRunner.withPropertyValues("spring.ldap.urls:ldap://localhost:123").run((context) -> {
			LdapContextSource contextSource = context.getBean(LdapContextSource.class);
			assertThat(contextSource.getUrls()).containsExactly("ldap://localhost:123");
		});
	}

	@Test
	void contextSourceWithSeveralUrls() {
		this.contextRunner.withPropertyValues("spring.ldap.urls:ldap://localhost:123,ldap://mycompany:123")
			.run((context) -> {
				LdapContextSource contextSource = context.getBean(LdapContextSource.class);
				LdapProperties ldapProperties = context.getBean(LdapProperties.class);
				assertThat(contextSource.getUrls()).containsExactly("ldap://localhost:123", "ldap://mycompany:123");
				assertThat(ldapProperties.getUrls()).hasSize(2);
			});
	}

	@Test
	void contextSourceWithUserDoesNotEnableAnonymousReadOnly() {
		this.contextRunner.withPropertyValues("spring.ldap.username:root").run((context) -> {
			LdapContextSource contextSource = context.getBean(LdapContextSource.class);
			assertThat(contextSource.getUserDn()).isEqualTo("root");
			assertThat(contextSource.isAnonymousReadOnly()).isFalse();
		});
	}

	@Test
	void contextSourceWithReferral() {
		this.contextRunner.withPropertyValues("spring.ldap.referral:ignore").run((context) -> {
			LdapContextSource contextSource = context.getBean(LdapContextSource.class);
			assertThat(contextSource).hasFieldOrPropertyWithValue("referral", "ignore");
		});
	}

	@Test
	void contextSourceWithExtraCustomization() {
		this.contextRunner
			.withPropertyValues("spring.ldap.urls:ldap://localhost:123", "spring.ldap.username:root",
					"spring.ldap.password:secret", "spring.ldap.anonymous-read-only:true",
					"spring.ldap.base:cn=SpringDevelopers",
					"spring.ldap.baseEnvironment.java.naming.security.authentication:DIGEST-MD5")
			.run((context) -> {
				LdapContextSource contextSource = context.getBean(LdapContextSource.class);
				assertThat(contextSource.getUserDn()).isEqualTo("root");
				assertThat(contextSource.getPassword()).isEqualTo("secret");
				assertThat(contextSource.isAnonymousReadOnly()).isTrue();
				assertThat(contextSource.getBaseLdapPathAsString()).isEqualTo("cn=SpringDevelopers");
				LdapProperties ldapProperties = context.getBean(LdapProperties.class);
				assertThat(ldapProperties.getBaseEnvironment()).containsEntry("java.naming.security.authentication",
						"DIGEST-MD5");
			});
	}

	@Test
	void contextSourceWithNoCustomization() {
		this.contextRunner.run((context) -> {
			LdapContextSource contextSource = context.getBean(LdapContextSource.class);
			assertThat(contextSource.getUserDn()).isEmpty();
			assertThat(contextSource.getPassword()).isEmpty();
			assertThat(contextSource.isAnonymousReadOnly()).isTrue();
			assertThat(contextSource.getBaseLdapPathAsString()).isEmpty();
		});
	}

	@Test
	void definesPropertiesBasedConnectionDetailsByDefault() {
		this.contextRunner.run((context) -> assertThat(context).hasSingleBean(PropertiesLdapConnectionDetails.class));
	}

	@Test
	void usesCustomConnectionDetailsWhenDefined() {
		this.contextRunner.withUserConfiguration(ConnectionDetailsConfiguration.class).run((context) -> {
			assertThat(context).hasSingleBean(LdapContextSource.class)
				.hasSingleBean(LdapConnectionDetails.class)
				.doesNotHaveBean(PropertiesLdapConnectionDetails.class);
			LdapContextSource contextSource = context.getBean(LdapContextSource.class);
			assertThat(contextSource.getUrls()).isEqualTo(new String[] { "ldaps://ldap.example.com" });
			assertThat(contextSource.getBaseLdapName()).isEqualTo(LdapUtils.newLdapName("dc=base"));
			assertThat(contextSource.getUserDn()).isEqualTo("ldap-user");
			assertThat(contextSource.getPassword()).isEqualTo("ldap-password");
		});
	}

	@Test
	void objectDirectoryMapperExists() {
		this.contextRunner.withPropertyValues("spring.ldap.urls:ldap://localhost:389").run((context) -> {
			assertThat(context).hasSingleBean(ObjectDirectoryMapper.class);
			ObjectDirectoryMapper objectDirectoryMapper = context.getBean(ObjectDirectoryMapper.class);
			assertThat(objectDirectoryMapper).extracting("converterManager")
				.extracting("conversionService", InstanceOfAssertFactories.type(ApplicationConversionService.class))
				.satisfies((conversionService) -> {
					assertThat(conversionService.canConvert(String.class, Name.class)).isTrue();
					assertThat(conversionService.canConvert(Name.class, String.class)).isTrue();
				});
		});
	}

	@Test
	void templateExists() {
		this.contextRunner.withPropertyValues("spring.ldap.urls:ldap://localhost:389").run((context) -> {
			assertThat(context).hasSingleBean(LdapTemplate.class);
			LdapTemplate ldapTemplate = context.getBean(LdapTemplate.class);
			assertThat(ldapTemplate).hasFieldOrPropertyWithValue("ignorePartialResultException", false);
			assertThat(ldapTemplate).hasFieldOrPropertyWithValue("ignoreNameNotFoundException", false);
			assertThat(ldapTemplate).hasFieldOrPropertyWithValue("ignoreSizeLimitExceededException", true);
			assertThat(ldapTemplate).extracting("objectDirectoryMapper")
				.isSameAs(context.getBean(ObjectDirectoryMapper.class));
		});
	}

	@Test
	void templateCanBeConfiguredWithCustomObjectDirectoryMapper() {
		ObjectDirectoryMapper objectDirectoryMapper = mock(ObjectDirectoryMapper.class);
		this.contextRunner.withPropertyValues("spring.ldap.urls:ldap://localhost:389")
			.withBean(ObjectDirectoryMapper.class, () -> objectDirectoryMapper)
			.run((context) -> {
				assertThat(context).hasSingleBean(LdapTemplate.class);
				LdapTemplate ldapTemplate = context.getBean(LdapTemplate.class);
				assertThat(ldapTemplate).extracting("objectDirectoryMapper").isSameAs(objectDirectoryMapper);
			});
	}

	@Test
	void templateConfigurationCanBeCustomized() {
		this.contextRunner
			.withPropertyValues("spring.ldap.urls:ldap://localhost:389",
					"spring.ldap.template.ignorePartialResultException=true",
					"spring.ldap.template.ignoreNameNotFoundException=true",
					"spring.ldap.template.ignoreSizeLimitExceededException=false")
			.run((context) -> {
				assertThat(context).hasSingleBean(LdapTemplate.class);
				LdapTemplate ldapTemplate = context.getBean(LdapTemplate.class);
				assertThat(ldapTemplate).hasFieldOrPropertyWithValue("ignorePartialResultException", true);
				assertThat(ldapTemplate).hasFieldOrPropertyWithValue("ignoreNameNotFoundException", true);
				assertThat(ldapTemplate).hasFieldOrPropertyWithValue("ignoreSizeLimitExceededException", false);
			});
	}

	@Test
	void contextSourceWithUserProvidedPooledContextSource() {
		this.contextRunner.withUserConfiguration(PooledContextSourceConfig.class).run((context) -> {
			LdapContextSource contextSource = context.getBean(LdapContextSource.class);
			assertThat(contextSource.getUrls()).containsExactly("ldap://localhost:389");
			assertThat(contextSource.isAnonymousReadOnly()).isTrue();
		});
	}

	@Test
	void contextSourceWithCustomUniqueDirContextAuthenticationStrategy() {
		this.contextRunner.withUserConfiguration(CustomDirContextAuthenticationStrategy.class).run((context) -> {
			assertThat(context).hasSingleBean(DirContextAuthenticationStrategy.class);
			LdapContextSource contextSource = context.getBean(LdapContextSource.class);
			assertThat(contextSource).extracting("authenticationStrategy")
				.isSameAs(context.getBean("customDirContextAuthenticationStrategy"));
		});
	}

	@Test
	void contextSourceWithCustomNonUniqueDirContextAuthenticationStrategy() {
		this.contextRunner
			.withUserConfiguration(CustomDirContextAuthenticationStrategy.class,
					AnotherCustomDirContextAuthenticationStrategy.class)
			.run((context) -> {
				assertThat(context).hasBean("customDirContextAuthenticationStrategy")
					.hasBean("anotherCustomDirContextAuthenticationStrategy");
				LdapContextSource contextSource = context.getBean(LdapContextSource.class);
				assertThat(contextSource).extracting("authenticationStrategy")
					.isNotSameAs(context.getBean("customDirContextAuthenticationStrategy"))
					.isNotSameAs(context.getBean("anotherCustomDirContextAuthenticationStrategy"))
					.isInstanceOf(SimpleDirContextAuthenticationStrategy.class);
			});
	}

	@Configuration(proxyBeanMethods = false)
	static class ConnectionDetailsConfiguration {

		@Bean
		LdapConnectionDetails ldapConnectionDetails() {
			return new LdapConnectionDetails() {

				@Override
				public String[] getUrls() {
					return new String[] { "ldaps://ldap.example.com" };
				}

				@Override
				public String getBase() {
					return "dc=base";
				}

				@Override
				public String getUsername() {
					return "ldap-user";
				}

				@Override
				public String getPassword() {
					return "ldap-password";
				}
			};
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class PooledContextSourceConfig {

		@Bean
		@Primary
		PooledContextSource pooledContextSource(LdapContextSource ldapContextSource) {
			PooledContextSource pooledContextSource = new PooledContextSource(new PoolConfig());
			pooledContextSource.setContextSource(ldapContextSource);
			return pooledContextSource;
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CustomDirContextAuthenticationStrategy {

		@Bean
		DirContextAuthenticationStrategy customDirContextAuthenticationStrategy() {
			return mock(DirContextAuthenticationStrategy.class);
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class AnotherCustomDirContextAuthenticationStrategy {

		@Bean
		DirContextAuthenticationStrategy anotherCustomDirContextAuthenticationStrategy() {
			return mock(DirContextAuthenticationStrategy.class);
		}

	}

}
