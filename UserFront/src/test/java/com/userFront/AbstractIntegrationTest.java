package com.userFront;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;

/**
 * Entry point for the smoke suite: every integration test extends this class.
 *
 * <p>Starts a single MySQL container for the whole JVM and feeds its JDBC
 * coordinates into the Spring context through {@link DatasourceInitializer}.
 * The schema is created from the entities ({@code ddl-auto=update}), so no
 * local MySQL is required — only Docker.
 *
 * <p>The container tracks the production major version (MySQL 5.7), which is
 * what Connector/J 5.1.x from the Boot 1.5 BOM and
 * {@code org.hibernate.dialect.MySQL5Dialect} are configured against.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = AbstractIntegrationTest.DatasourceInitializer.class)
public abstract class AbstractIntegrationTest {

	private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:5.7")
			.withDatabaseName("onlinebanking")
			.withUrlParam("useSSL", "false");

	static {
		MYSQL.start();
	}

	public static class DatasourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext context) {
			TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
					"spring.datasource.url=" + MYSQL.getJdbcUrl(),
					"spring.datasource.username=" + MYSQL.getUsername(),
					"spring.datasource.password=" + MYSQL.getPassword(),
					"spring.jpa.hibernate.ddl-auto=update");
		}
	}
}
