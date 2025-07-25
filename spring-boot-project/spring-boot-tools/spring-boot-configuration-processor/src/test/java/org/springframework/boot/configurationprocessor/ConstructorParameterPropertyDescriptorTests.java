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

package org.springframework.boot.configurationprocessor;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import org.junit.jupiter.api.Test;

import org.springframework.boot.configurationsample.immutable.ImmutableCollectionProperties;
import org.springframework.boot.configurationsample.immutable.ImmutableInnerClassProperties;
import org.springframework.boot.configurationsample.immutable.ImmutablePrimitiveProperties;
import org.springframework.boot.configurationsample.immutable.ImmutablePrimitiveWithDefaultsProperties;
import org.springframework.boot.configurationsample.immutable.ImmutablePrimitiveWrapperWithDefaultsProperties;
import org.springframework.boot.configurationsample.immutable.ImmutableSimpleProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConstructorParameterPropertyDescriptor}.
 *
 * @author Stephane Nicoll
 */
class ConstructorParameterPropertyDescriptorTests extends PropertyDescriptorTests {

	@Test
	void constructorParameterSimpleProperty() {
		process(ImmutableSimpleProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableSimpleProperties.class);
			ConstructorParameterPropertyDescriptor property = createPropertyDescriptor(ownerElement, "theName");
			assertThat(property.getName()).isEqualTo("theName");
			assertThat(property.getParameter()).hasToString("theName");
			assertThat(property.getGetter().getSimpleName()).hasToString("getTheName");
			assertThat(property.isProperty(metadataEnv)).isTrue();
			assertThat(property.isNested(metadataEnv)).isFalse();
		});
	}

	@Test
	void constructorParameterNestedPropertySameClass() {
		process(ImmutableInnerClassProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableInnerClassProperties.class);
			ConstructorParameterPropertyDescriptor property = createPropertyDescriptor(ownerElement, "first");
			assertThat(property.getName()).isEqualTo("first");
			assertThat(property.getParameter()).hasToString("first");
			assertThat(property.getGetter().getSimpleName()).hasToString("getFirst");
			assertThat(property.isProperty(metadataEnv)).isFalse();
			assertThat(property.isNested(metadataEnv)).isTrue();
		});
	}

	@Test
	void constructorParameterNestedPropertyWithAnnotation() {
		process(ImmutableInnerClassProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableInnerClassProperties.class);
			ConstructorParameterPropertyDescriptor property = createPropertyDescriptor(ownerElement, "third");
			assertThat(property.getName()).isEqualTo("third");
			assertThat(property.getParameter()).hasToString("third");
			assertThat(property.getGetter().getSimpleName()).hasToString("getThird");
			assertThat(property.isProperty(metadataEnv)).isFalse();
			assertThat(property.isNested(metadataEnv)).isTrue();
		});
	}

	@Test
	void constructorParameterSimplePropertyWithNoAccessorShouldBeExposed() {
		process(ImmutableSimpleProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableSimpleProperties.class);
			ConstructorParameterPropertyDescriptor property = createPropertyDescriptor(ownerElement, "counter");
			assertThat(property.getName()).isEqualTo("counter");
			assertThat(property.getParameter()).hasToString("counter");
			assertThat(property.getGetter()).isNull();
			assertThat(property.isProperty(metadataEnv)).isTrue();
			assertThat(property.isNested(metadataEnv)).isFalse();
		});
	}

	@Test
	void constructorParameterMetadataSimpleProperty() {
		process(ImmutableSimpleProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableSimpleProperties.class);
			ConstructorParameterPropertyDescriptor property = createPropertyDescriptor(ownerElement, "counter");
			assertItemMetadata(metadataEnv, property).isProperty()
				.hasName("test.counter")
				.hasType(Long.class)
				.hasSourceType(ImmutableSimpleProperties.class)
				.hasNoDescription()
				.isNotDeprecated();
		});
	}

	@Test
	void constructorParameterMetadataNestedGroup() {
		process(ImmutableInnerClassProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableInnerClassProperties.class);
			ConstructorParameterPropertyDescriptor property = createPropertyDescriptor(ownerElement, "first");
			assertItemMetadata(metadataEnv, property).isGroup()
				.hasName("test.first")
				.hasType("org.springframework.boot.configurationsample.immutable.ImmutableInnerClassProperties$Foo")
				.hasSourceType(ImmutableInnerClassProperties.class)
				.hasSourceMethod("getFirst()")
				.hasNoDescription()
				.isNotDeprecated();
		});
	}

	@Test
	void constructorParameterDeprecatedPropertyOnGetter() {
		process(ImmutableSimpleProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableSimpleProperties.class);
			ExecutableElement getter = getMethod(ownerElement, "isFlag");
			VariableElement field = getField(ownerElement, "flag");
			VariableElement constructorParameter = getConstructorParameter(ownerElement, "flag");
			ConstructorParameterPropertyDescriptor property = new ConstructorParameterPropertyDescriptor("flag",
					field.asType(), constructorParameter, ownerElement, getter, null, field);
			assertItemMetadata(metadataEnv, property).isProperty().isDeprecatedWithNoInformation();
		});
	}

	@Test
	void constructorParameterPropertyWithDescription() {
		process(ImmutableSimpleProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableSimpleProperties.class);
			ConstructorParameterPropertyDescriptor property = createPropertyDescriptor(ownerElement, "theName");
			assertItemMetadata(metadataEnv, property).isProperty()
				.hasDescription("The name of this simple properties.");
		});
	}

	@Test
	void constructorParameterPropertyWithDefaultValue() {
		process(ImmutableSimpleProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableSimpleProperties.class);
			ConstructorParameterPropertyDescriptor property = createPropertyDescriptor(ownerElement, "theName");
			assertItemMetadata(metadataEnv, property).isProperty().hasDefaultValue("boot");
		});
	}

	@Test
	void constructorParameterPropertyWithPrimitiveTypes() {
		process(ImmutablePrimitiveProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutablePrimitiveProperties.class);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "flag")).hasDefaultValue(false);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "octet")).hasDefaultValue((byte) 0);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "letter")).hasDefaultValue(null);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "number"))
				.hasDefaultValue((short) 0);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "counter")).hasDefaultValue(0);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "value")).hasDefaultValue(0L);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "percentage")).hasDefaultValue(0F);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "ratio")).hasDefaultValue(0D);
		});
	}

	@Test
	void constructorParameterPropertyWithPrimitiveTypesAndDefaultValues() {
		process(ImmutablePrimitiveWithDefaultsProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutablePrimitiveWithDefaultsProperties.class);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "flag")).hasDefaultValue(true);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "octet"))
				.hasDefaultValue((byte) 120);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "letter")).hasDefaultValue("a");
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "number"))
				.hasDefaultValue((short) 1000);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "counter")).hasDefaultValue(42);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "value")).hasDefaultValue(2000L);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "percentage")).hasDefaultValue(0.5F);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "ratio")).hasDefaultValue(42.42);
		});
	}

	@Test
	void constructorParameterPropertyWithPrimitiveWrapperTypesAndDefaultValues() {
		process(ImmutablePrimitiveWrapperWithDefaultsProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutablePrimitiveWrapperWithDefaultsProperties.class);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "flag")).hasDefaultValue(true);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "octet"))
				.hasDefaultValue((byte) 120);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "letter")).hasDefaultValue("a");
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "number"))
				.hasDefaultValue((short) 1000);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "counter")).hasDefaultValue(42);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "value")).hasDefaultValue(2000L);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "percentage")).hasDefaultValue(0.5F);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "ratio")).hasDefaultValue(42.42);
		});
	}

	@Test
	void constructorParameterPropertyWithCollectionTypesAndDefaultValues() {
		process(ImmutableCollectionProperties.class, (roundEnv, metadataEnv) -> {
			TypeElement ownerElement = roundEnv.getRootElement(ImmutableCollectionProperties.class);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "names")).hasDefaultValue(null);
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "flags"))
				.hasDefaultValue(Arrays.asList(true, false));
			assertItemMetadata(metadataEnv, createPropertyDescriptor(ownerElement, "durations"))
				.hasDefaultValue(Arrays.asList("10s", "1m", "1h"));
		});
	}

	protected ConstructorParameterPropertyDescriptor createPropertyDescriptor(TypeElement ownerElement, String name) {
		VariableElement constructorParameter = getConstructorParameter(ownerElement, name);
		VariableElement field = getField(ownerElement, name);
		ExecutableElement getter = getMethod(ownerElement, createAccessorMethodName("get", name));
		ExecutableElement setter = getMethod(ownerElement, createAccessorMethodName("set", name));
		return new ConstructorParameterPropertyDescriptor(name, field.asType(), constructorParameter, ownerElement,
				getter, setter, field);
	}

	private VariableElement getConstructorParameter(TypeElement ownerElement, String name) {
		List<ExecutableElement> constructors = ElementFilter.constructorsIn(ownerElement.getEnclosedElements())
			.stream()
			.filter((constructor) -> !constructor.getParameters().isEmpty())
			.toList();
		if (constructors.size() != 1) {
			throw new IllegalStateException("No candidate constructor for " + ownerElement);
		}
		return constructors.get(0)
			.getParameters()
			.stream()
			.filter((parameter) -> parameter.getSimpleName().toString().equals(name))
			.findFirst()
			.orElse(null);
	}

}
