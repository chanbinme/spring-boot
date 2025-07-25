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

package org.springframework.boot.actuate.session;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.session.SessionsDescriptor.SessionDescriptor;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SessionsEndpoint}.
 *
 * @author Vedran Pavic
 */
class SessionsEndpointTests {

	private static final Session session = new MapSession();

	@SuppressWarnings("unchecked")
	private final SessionRepository<Session> sessionRepository = mock(SessionRepository.class);

	@SuppressWarnings("unchecked")
	private final FindByIndexNameSessionRepository<Session> indexedSessionRepository = mock(
			FindByIndexNameSessionRepository.class);

	private final SessionsEndpoint endpoint = new SessionsEndpoint(this.sessionRepository,
			this.indexedSessionRepository);

	@Test
	void sessionsForUsername() {
		given(this.indexedSessionRepository.findByPrincipalName("user"))
			.willReturn(Collections.singletonMap(session.getId(), session));
		List<SessionDescriptor> result = this.endpoint.sessionsForUsername("user").getSessions();
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(session.getId());
		assertThat(result.get(0).getAttributeNames()).isEqualTo(session.getAttributeNames());
		assertThat(result.get(0).getCreationTime()).isEqualTo(session.getCreationTime());
		assertThat(result.get(0).getLastAccessedTime()).isEqualTo(session.getLastAccessedTime());
		assertThat(result.get(0).getMaxInactiveInterval()).isEqualTo(session.getMaxInactiveInterval().getSeconds());
		assertThat(result.get(0).isExpired()).isEqualTo(session.isExpired());
		then(this.indexedSessionRepository).should().findByPrincipalName("user");
	}

	@Test
	void sessionsForUsernameWhenNoIndexedRepository() {
		SessionsEndpoint endpoint = new SessionsEndpoint(this.sessionRepository, null);
		assertThat(endpoint.sessionsForUsername("user")).isNull();
	}

	@Test
	void getSession() {
		given(this.sessionRepository.findById(session.getId())).willReturn(session);
		SessionDescriptor result = this.endpoint.getSession(session.getId());
		assertThat(result.getId()).isEqualTo(session.getId());
		assertThat(result.getAttributeNames()).isEqualTo(session.getAttributeNames());
		assertThat(result.getCreationTime()).isEqualTo(session.getCreationTime());
		assertThat(result.getLastAccessedTime()).isEqualTo(session.getLastAccessedTime());
		assertThat(result.getMaxInactiveInterval()).isEqualTo(session.getMaxInactiveInterval().getSeconds());
		assertThat(result.isExpired()).isEqualTo(session.isExpired());
		then(this.sessionRepository).should().findById(session.getId());
	}

	@Test
	void getSessionWithIdNotFound() {
		given(this.sessionRepository.findById("not-found")).willReturn(null);
		assertThat(this.endpoint.getSession("not-found")).isNull();
		then(this.sessionRepository).should().findById("not-found");
	}

	@Test
	void deleteSession() {
		this.endpoint.deleteSession(session.getId());
		then(this.sessionRepository).should().deleteById(session.getId());
	}

}
