package com.userFront;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

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

	/** Docker Engine 25+ rejects the API 1.32 that Testcontainers 1.19.x requests by default. */
	private static final String PREFERRED_DOCKER_API_VERSION = "1.44";

	static {
		if (System.getProperty("api.version") == null && System.getenv("DOCKER_API_VERSION") == null) {
			System.setProperty("api.version", dockerApiVersion());
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

	/**
	 * {@value #PREFERRED_DOCKER_API_VERSION}, capped at the daemon's maximum API version (as
	 * reported by the {@code docker} CLI) so older engines such as Docker 24 (API 1.43) still work.
	 */
	private static String dockerApiVersion() {
		try {
			Process process = new ProcessBuilder("docker", "version", "--format", "{{.Server.APIVersion}}")
					.redirectErrorStream(true)
					.start();
			if (!process.waitFor(10, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				return PREFERRED_DOCKER_API_VERSION;
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String serverMax = reader.readLine();
				if (process.exitValue() == 0 && serverMax != null && serverMax.trim().matches("\\d+\\.\\d+")) {
					return compareApiVersions(serverMax.trim(), PREFERRED_DOCKER_API_VERSION) < 0
							? serverMax.trim()
							: PREFERRED_DOCKER_API_VERSION;
				}
			}
		}
		catch (IOException e) {
			// No docker CLI on the PATH: fall back to the preferred version.
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return PREFERRED_DOCKER_API_VERSION;
	}

	private static int compareApiVersions(String a, String b) {
		String[] x = a.split("\\.");
		String[] y = b.split("\\.");
		int major = Integer.compare(Integer.parseInt(x[0]), Integer.parseInt(y[0]));
		return major != 0 ? major : Integer.compare(Integer.parseInt(x[1]), Integer.parseInt(y[1]));
	}

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
