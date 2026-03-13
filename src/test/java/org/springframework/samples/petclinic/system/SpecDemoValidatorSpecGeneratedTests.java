package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecDemoValidatorSpecGeneratedTests {

	private final SpecDemoValidator subject = new SpecDemoValidator();

	@Test
	void test_validatevisitrequest_valid_baseline() {
		assertThatCode(() -> subject.validateVisitRequest(1, "checkup", "MEDIUM")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_petid_below_min() {
		assertThatThrownBy(() -> subject.validateVisitRequest(0, "checkup", "MEDIUM")).isInstanceOf(Exception.class);
	}

	@Test
	void test_validatevisitrequest_petid_at_min() {
		assertThatCode(() -> subject.validateVisitRequest(1, "checkup", "MEDIUM")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_petid_above_min() {
		assertThatCode(() -> subject.validateVisitRequest(2, "checkup", "MEDIUM")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_petid_below_max() {
		assertThatCode(() -> subject.validateVisitRequest(99999, "checkup", "MEDIUM")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_petid_at_max() {
		assertThatCode(() -> subject.validateVisitRequest(100000, "checkup", "MEDIUM")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_petid_above_max() {
		assertThatThrownBy(() -> subject.validateVisitRequest(100001, "checkup", "MEDIUM"))
			.isInstanceOf(Exception.class);
	}

	@Test
	void test_validatevisitrequest_petid_equivalence_valid() {
		assertThatCode(() -> subject.validateVisitRequest(1, "checkup", "MEDIUM")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_petid_equivalence_invalid() {
		assertThatThrownBy(() -> subject.validateVisitRequest(0, "checkup", "MEDIUM")).isInstanceOf(Exception.class);
	}

	@Test
	void test_validatevisitrequest_description_equivalence_valid() {
		assertThatCode(() -> subject.validateVisitRequest(1, "checkup", "MEDIUM")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_description_equivalence_invalid() {
		assertThatThrownBy(() -> subject.validateVisitRequest(1, "", "MEDIUM")).isInstanceOf(Exception.class);
	}

	@Test
	void test_validatevisitrequest_priority_enum_valid() {
		assertThatCode(() -> subject.validateVisitRequest(1, "checkup", "LOW")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_priority_enum_invalid() {
		assertThatThrownBy(() -> subject.validateVisitRequest(1, "checkup", "INVALID_ENUM_VALUE"))
			.isInstanceOf(Exception.class);
	}

	@Test
	void test_validatevisitrequest_priority_equivalence_valid() {
		assertThatCode(() -> subject.validateVisitRequest(1, "checkup", "MEDIUM")).doesNotThrowAnyException();
	}

	@Test
	void test_validatevisitrequest_priority_equivalence_invalid() {
		assertThatThrownBy(() -> subject.validateVisitRequest(1, "checkup", "URGENT")).isInstanceOf(Exception.class);
	}

}