package com.userFront;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Entry point for the UserFront smoke suite: every {@code @SpringBootTest} extends this class.
 *
 * <p>Boots the whole application against a MySQL Testcontainer, so {@code mvn verify} needs only
 * Docker, not a local MySQL. A single container is started once per JVM and its JDBC coordinates
 * are pushed into the environment by {@link DataSourceInitializer} before the context refreshes
 * ({@code spring-boot-testcontainers} / {@code @ServiceConnection} only exist from Boot 3.1).
 * The schema is created from the JPA entities with {@code spring.jpa.hibernate.ddl-auto=update}.
 *
 * <p>The image defaults to {@value #DEFAULT_MYSQL_IMAGE}, the assumed production major version,
 * and can be overridden with {@code -Dtest.mysql.image=mysql:5.7}. The server is started with
 * {@code mysql_native_password} because the Boot 1.5 stack ships Connector/J 5.1, which cannot
 * speak {@code caching_sha2_password}.
 *
 * <p>Subclasses get an autoconfigured {@link MockMvc} (with the Spring Security filter chain)
 * as {@link #mockMvc}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserFrontApplication.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = AbstractIntegrationTest.DataSourceInitializer.class)
public abstract class AbstractIntegrationTest {

	public static final String DEFAULT_MYSQL_IMAGE = "mysql:8.0";

	static {
		// Testcontainers 1.19.x pins docker-java to API 1.32, which Docker Engine 25+ rejects.
		if (System.getProperty("api.version") == null && System.getenv("DOCKER_API_VERSION") == null) {
			System.setProperty("api.version", "1.44");
		}
	}

	protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>(
			DockerImageName.parse(System.getProperty("test.mysql.image", DEFAULT_MYSQL_IMAGE))
					.asCompatibleSubstituteFor("mysql"))
			.withDatabaseName("onlinebanking")
			.withUrlParam("useSSL", "false")
			.withCommand("--default-authentication-plugin=mysql_native_password");

	static {
		MYSQL.start();
	}

	@Autowired
	protected MockMvc mockMvc;

	public static class DataSourceInitializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			EnvironmentTestUtils.addEnvironment(applicationContext,
					"spring.datasource.url=" + MYSQL.getJdbcUrl(),
					"spring.datasource.username=" + MYSQL.getUsername(),
					"spring.datasource.password=" + MYSQL.getPassword(),
					"spring.datasource.driver-class-name=" + MYSQL.getDriverClassName(),
					"spring.jpa.hibernate.ddl-auto=update");
		}
	}
}
