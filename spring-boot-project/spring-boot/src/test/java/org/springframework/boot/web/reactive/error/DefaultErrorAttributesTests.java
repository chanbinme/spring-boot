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

package org.springframework.boot.web.reactive.error;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link DefaultErrorAttributes}.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Scott Frederick
 * @author Moritz Halbritter
 * @author Yanming Zhou
 * @author Yongjun Hong
 */
class DefaultErrorAttributesTests {

	private static final ResponseStatusException NOT_FOUND = new ResponseStatusException(HttpStatus.NOT_FOUND);

	private DefaultErrorAttributes errorAttributes = new DefaultErrorAttributes();

	private final List<HttpMessageReader<?>> readers = ServerCodecConfigurer.create().getReaders();

	@Test
	void missingExceptionAttribute() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
		ServerRequest request = ServerRequest.create(exchange, this.readers);
		assertThatIllegalStateException()
			.isThrownBy(() -> this.errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults()))
			.withMessageContaining("Missing exception attribute in ServerWebExchange");
	}

	@Test
	void includeTimeStamp() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, NOT_FOUND),
				ErrorAttributeOptions.defaults());
		assertThat(attributes.get("timestamp")).isInstanceOf(Date.class);
	}

	@Test
	void defaultStatusCode() {
		Error error = new OutOfMemoryError("Test error");
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, error),
				ErrorAttributeOptions.defaults());
		assertThat(attributes).containsEntry("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		assertThat(attributes).containsEntry("status", 500);
	}

	@Test
	void annotatedResponseStatusCode() {
		Exception error = new CustomException();
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, error),
				ErrorAttributeOptions.defaults());
		assertThat(attributes).containsEntry("error", HttpStatus.I_AM_A_TEAPOT.getReasonPhrase());
		assertThat(attributes).doesNotContainKey("message");
		assertThat(attributes).containsEntry("status", HttpStatus.I_AM_A_TEAPOT.value());
	}

	@Test
	void annotatedResponseStatusCodeWithExceptionMessage() {
		Exception error = new CustomException("Test Message");
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, error),
				ErrorAttributeOptions.of(Include.MESSAGE, Include.STATUS, Include.ERROR));
		assertThat(attributes).containsEntry("error", HttpStatus.I_AM_A_TEAPOT.getReasonPhrase());
		assertThat(attributes).containsEntry("message", "Test Message");
		assertThat(attributes).containsEntry("status", HttpStatus.I_AM_A_TEAPOT.value());
	}

	@Test
	void annotatedResponseStatusCodeWithCustomReasonPhrase() {
		Exception error = new Custom2Exception();
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, error),
				ErrorAttributeOptions.of(Include.MESSAGE, Include.STATUS, Include.ERROR));
		assertThat(attributes).containsEntry("error", HttpStatus.I_AM_A_TEAPOT.getReasonPhrase());
		assertThat(attributes).containsEntry("status", HttpStatus.I_AM_A_TEAPOT.value());
		assertThat(attributes).containsEntry("message", "Nope!");
	}

	@Test
	void includeStatusCode() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, NOT_FOUND),
				ErrorAttributeOptions.defaults());
		assertThat(attributes).containsEntry("error", HttpStatus.NOT_FOUND.getReasonPhrase());
		assertThat(attributes).containsEntry("status", 404);
	}

	@Test
	void getError() {
		Error error = new OutOfMemoryError("Test error");
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		ServerRequest serverRequest = buildServerRequest(request, error);
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(serverRequest,
				ErrorAttributeOptions.of(Include.MESSAGE));
		assertThat(this.errorAttributes.getError(serverRequest)).isSameAs(error);
		assertThat(attributes).doesNotContainKey("exception");
		assertThat(attributes).containsEntry("message", "Test error");
	}

	@Test
	void excludeMessage() {
		Error error = new OutOfMemoryError("Test error");
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		ServerRequest serverRequest = buildServerRequest(request, error);
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(serverRequest,
				ErrorAttributeOptions.defaults());
		assertThat(this.errorAttributes.getError(serverRequest)).isSameAs(error);
		assertThat(attributes).doesNotContainKey("message");
	}

	@Test
	void includeException() {
		RuntimeException error = new RuntimeException("Test");
		this.errorAttributes = new DefaultErrorAttributes();
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		ServerRequest serverRequest = buildServerRequest(request, error);
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(serverRequest,
				ErrorAttributeOptions.of(Include.EXCEPTION, Include.MESSAGE));
		assertThat(this.errorAttributes.getError(serverRequest)).isSameAs(error);
		assertThat(attributes).containsEntry("exception", RuntimeException.class.getName());
		assertThat(attributes).containsEntry("message", "Test");
	}

	@Test
	void processResponseStatusException() {
		RuntimeException nested = new RuntimeException("Test");
		ResponseStatusException error = new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request", nested);
		this.errorAttributes = new DefaultErrorAttributes();
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		ServerRequest serverRequest = buildServerRequest(request, error);
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(serverRequest,
				ErrorAttributeOptions.of(Include.EXCEPTION, Include.MESSAGE, Include.STATUS));
		assertThat(attributes).containsEntry("status", 400);
		assertThat(attributes).containsEntry("message", "invalid request");
		assertThat(attributes).containsEntry("exception", RuntimeException.class.getName());
		assertThat(this.errorAttributes.getError(serverRequest)).isSameAs(error);
	}

	@Test
	void processResponseStatusExceptionWithNoNestedCause() {
		ResponseStatusException error = new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
				"could not process request");
		this.errorAttributes = new DefaultErrorAttributes();
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		ServerRequest serverRequest = buildServerRequest(request, error);
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(serverRequest,
				ErrorAttributeOptions.of(Include.EXCEPTION, Include.MESSAGE, Include.STATUS));
		assertThat(attributes).containsEntry("status", 406);
		assertThat(attributes).containsEntry("message", "could not process request");
		assertThat(attributes).containsEntry("exception", ResponseStatusException.class.getName());
		assertThat(this.errorAttributes.getError(serverRequest)).isSameAs(error);
	}

	@Test
	void notIncludeTrace() {
		RuntimeException ex = new RuntimeException("Test");
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, ex),
				ErrorAttributeOptions.defaults());
		assertThat(attributes).doesNotContainKey("trace");
	}

	@Test
	void includeTrace() {
		RuntimeException ex = new RuntimeException("Test");
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, ex),
				ErrorAttributeOptions.of(Include.STACK_TRACE));
		assertThat(attributes.get("trace").toString()).startsWith("java.lang");
	}

	@Test
	void includePathByDefault() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, NOT_FOUND),
				ErrorAttributeOptions.defaults());
		assertThat(attributes).containsEntry("path", "/test");
	}

	@Test
	void includePath() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, NOT_FOUND),
				ErrorAttributeOptions.of(Include.PATH));
		assertThat(attributes).containsEntry("path", "/test");
	}

	@Test
	void pathShouldIncludeContext() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/context/test").contextPath("/context").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, NOT_FOUND),
				ErrorAttributeOptions.of(Include.PATH));
		assertThat(attributes).containsEntry("path", "/context/test");
	}

	@Test
	void excludePath() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, NOT_FOUND),
				ErrorAttributeOptions.of());
		assertThat(attributes).doesNotContainEntry("path", "/test");
	}

	@Test
	void includeLogPrefix() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		ServerRequest serverRequest = buildServerRequest(request, NOT_FOUND);
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(serverRequest,
				ErrorAttributeOptions.defaults());
		assertThat(attributes).containsEntry("requestId", serverRequest.exchange().getRequest().getId());
	}

	@Test
	void extractBindingResultErrors() throws Exception {
		Method method = getClass().getDeclaredMethod("method", String.class);
		MethodParameter stringParam = new MethodParameter(method, 0);
		BindingResult bindingResult = new MapBindingResult(Collections.singletonMap("a", "b"), "objectName");
		bindingResult.addError(new ObjectError("c", "d"));
		Exception ex = new WebExchangeBindException(stringParam, bindingResult);
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, ex),
				ErrorAttributeOptions.of(Include.MESSAGE, Include.BINDING_ERRORS));
		assertThat(attributes.get("message")).asString()
			.startsWith("Validation failed for argument at index 0 in method: "
					+ "int org.springframework.boot.web.reactive.error.DefaultErrorAttributesTests"
					+ ".method(java.lang.String), with 1 error(s)");
		assertThat(attributes).containsEntry("errors",
				org.springframework.boot.web.error.Error.wrap(bindingResult.getAllErrors()));
	}

	@Test
	void extractBindingResultErrorsThatCausedAResponseStatusException() throws Exception {
		Method method = getClass().getDeclaredMethod("method", String.class);
		MethodParameter stringParam = new MethodParameter(method, 0);
		BindingResult bindingResult = new MapBindingResult(Collections.singletonMap("a", "b"), "objectName");
		bindingResult.addError(new ObjectError("c", "d"));
		Exception ex = new WebExchangeBindException(stringParam, bindingResult);
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(
				buildServerRequest(request, new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid", ex)),
				ErrorAttributeOptions.of(Include.MESSAGE, Include.BINDING_ERRORS));
		assertThat(attributes.get("message")).isEqualTo("Invalid");
		assertThat(attributes).containsEntry("errors",
				org.springframework.boot.web.error.Error.wrap(bindingResult.getAllErrors()));
	}

	@Test
	void extractMethodValidationResultErrors() throws Exception {
		Object target = "test";
		Method method = String.class.getMethod("substring", int.class);
		MethodParameter parameter = new MethodParameter(method, 0);
		MethodValidationResult methodValidationResult = MethodValidationResult.create(target, method,
				List.of(new ParameterValidationResult(parameter, -1,
						List.of(new ObjectError("beginIndex", "beginIndex is negative")), null, null, null,
						(error, sourceType) -> {
							throw new IllegalArgumentException("No source object of the given type");
						})));
		HandlerMethodValidationException ex = new HandlerMethodValidationException(methodValidationResult);
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, ex),
				ErrorAttributeOptions.of(Include.MESSAGE, Include.BINDING_ERRORS));
		assertThat(attributes.get("message")).asString()
			.isEqualTo(
					"Validation failed for method='public java.lang.String java.lang.String.substring(int)'. Error count: 1");
		assertThat(attributes).containsEntry("errors",
				org.springframework.boot.web.error.Error.wrap(methodValidationResult.getAllErrors()));
	}

	@Test
	void extractBindingResultErrorsExcludeMessageAndErrors() throws Exception {
		Method method = getClass().getDeclaredMethod("method", String.class);
		MethodParameter stringParam = new MethodParameter(method, 0);
		BindingResult bindingResult = new MapBindingResult(Collections.singletonMap("a", "b"), "objectName");
		bindingResult.addError(new ObjectError("c", "d"));
		Exception ex = new WebExchangeBindException(stringParam, bindingResult);
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, ex),
				ErrorAttributeOptions.defaults());
		assertThat(attributes).doesNotContainKey("message");
		assertThat(attributes).doesNotContainKey("errors");
	}

	@Test
	void extractParameterValidationResultErrors() throws Exception {
		Object target = "test";
		Method method = String.class.getMethod("substring", int.class);
		MethodParameter parameter = new MethodParameter(method, 0);
		ParameterValidationResult parameterValidationResult = new ParameterValidationResult(parameter, -1,
				List.of(new ObjectError("beginIndex", "beginIndex is negative")), null, null, null,
				(error, sourceType) -> {
					throw new IllegalArgumentException("No source object of the given type");
				});
		MethodValidationResult methodValidationResult = MethodValidationResult.create(target, method,
				List.of(parameterValidationResult));
		HandlerMethodValidationException ex = new HandlerMethodValidationException(methodValidationResult);
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(buildServerRequest(request, ex),
				ErrorAttributeOptions.of(Include.MESSAGE, Include.BINDING_ERRORS));
		assertThat(attributes.get("message")).asString()
			.isEqualTo(
					"Validation failed for method='public java.lang.String java.lang.String.substring(int)'. Error count: 1");
		assertThat(attributes).containsEntry("errors",
				org.springframework.boot.web.error.Error.wrap(methodValidationResult.getAllErrors()));
	}

	@Test
	void excludeStatus() {
		ResponseStatusException error = new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
				"could not process request");
		this.errorAttributes = new DefaultErrorAttributes();
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		ServerRequest serverRequest = buildServerRequest(request, error);
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(serverRequest,
				ErrorAttributeOptions.defaults().excluding(Include.STATUS));
		assertThat(attributes).doesNotContainKey("status");
	}

	@Test
	void excludeError() {
		ResponseStatusException error = new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
				"could not process request");
		this.errorAttributes = new DefaultErrorAttributes();
		MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
		ServerRequest serverRequest = buildServerRequest(request, error);
		Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(serverRequest,
				ErrorAttributeOptions.defaults().excluding(Include.ERROR));
		assertThat(attributes).doesNotContainKey("error");
	}

	private ServerRequest buildServerRequest(MockServerHttpRequest request, Throwable error) {
		ServerWebExchange exchange = MockServerWebExchange.from(request);
		this.errorAttributes.storeErrorInformation(error, exchange);
		return ServerRequest.create(exchange, this.readers);
	}

	int method(String firstParam) {
		return 42;
	}

	@ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
	static class CustomException extends RuntimeException {

		CustomException() {
		}

		CustomException(String message) {
			super(message);
		}

	}

	@ResponseStatus(value = HttpStatus.I_AM_A_TEAPOT, reason = "Nope!")
	static class Custom2Exception extends RuntimeException {

	}

}
