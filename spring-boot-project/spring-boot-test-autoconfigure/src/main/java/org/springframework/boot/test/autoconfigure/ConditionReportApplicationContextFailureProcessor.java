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

package org.springframework.boot.test.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ApplicationContextFailureProcessor;

/**
 * An {@link ApplicationContextFailureProcessor} that prints the
 * {@link ConditionEvaluationReport} when the context cannot be prepared.
 *
 * @author Phillip Webb
 * @author Scott Frederick
 * @since 3.0.0
 * @deprecated in 3.2.11 for removal in 4.0.0
 */
@Deprecated(since = "3.2.11", forRemoval = true)
public class ConditionReportApplicationContextFailureProcessor implements ApplicationContextFailureProcessor {

	@Override
	public void processLoadFailure(ApplicationContext context, Throwable exception) {
		if (context instanceof ConfigurableApplicationContext configurableContext) {
			ConditionEvaluationReport report = ConditionEvaluationReport.get(configurableContext.getBeanFactory());
			System.err.println(new ConditionEvaluationReportMessage(report));
		}
	}

}
