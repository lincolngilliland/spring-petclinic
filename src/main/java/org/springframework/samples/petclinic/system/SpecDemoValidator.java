/*
 * Copyright 2012-2025 the original author or authors.
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

package org.springframework.samples.petclinic.system;

/**
 * Demo target for specification-based test generation.
 */
public class SpecDemoValidator {

	public void validateVisitRequest(int petId, String description, String priority) {
		if (petId < 1 || petId > 100_000) {
			throw new IllegalArgumentException("petId out of range");
		}
		if (description == null || description.trim().isEmpty()) {
			throw new IllegalArgumentException("description required");
		}
		if (!"LOW".equals(priority) && !"MEDIUM".equals(priority) && !"HIGH".equals(priority)) {
			throw new IllegalArgumentException("invalid priority");
		}
	}

}
