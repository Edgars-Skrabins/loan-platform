package io.github.edgarsskrabins.loan_platform;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("""
		Needs a live Postgres: application.yaml points at localhost:5432 and jpa.hibernate.ddl-auto
		is 'validate', so the context cannot start without `docker compose up` and the Flyway
		migration having run. Worth replacing with a Testcontainers-backed @SpringBootTest so it
		runs on a clean checkout and in CI.""")
class LoanPlatformApplicationTests {

	@Test
	void contextLoads() {
	}

}
