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

package org.springframework.boot.testsupport.gradle.testkit;

import java.util.Arrays;
import java.util.List;

import org.gradle.api.JavaVersion;
import org.gradle.util.GradleVersion;

/**
 * Versions of Gradle used for testing.
 *
 * @author Scott Frederick
 */
public final class GradleVersions {

	private GradleVersions() {
	}

	public static List<String> allCompatible() {
		if (isJavaVersion(JavaVersion.VERSION_24)) {
			return Arrays.asList(GradleVersion.current().getVersion(), "9.0.0-milestone-9");
		}
		if (isJavaVersion(JavaVersion.VERSION_23)) {
			return Arrays.asList(GradleVersion.current().getVersion(), "9.0.0-milestone-9");
		}
		if (isJavaVersion(JavaVersion.VERSION_22)) {
			return Arrays.asList("8.8", GradleVersion.current().getVersion(), "9.0.0-milestone-9");
		}
		if (isJavaVersion(JavaVersion.VERSION_21)) {
			return Arrays.asList("8.5", GradleVersion.current().getVersion(), "9.0.0-milestone-9");
		}
		return Arrays.asList("7.6.5", "8.4", GradleVersion.current().getVersion(), "9.0.0-milestone-9");
	}

	public static String minimumCompatible() {
		return allCompatible().get(0);
	}

	public static String maximumCompatible() {
		List<String> versions = allCompatible();
		return versions.get(versions.size() - 1);
	}

	private static boolean isJavaVersion(JavaVersion version) {
		return JavaVersion.current().isCompatibleWith(version);
	}

}
