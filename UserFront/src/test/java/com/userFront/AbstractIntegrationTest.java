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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserFrontApplication.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = AbstractIntegrationTest.DataSourceInitializer.class)
public abstract class AbstractIntegrationTest {

	static {
		// docker-java defaults to API 1.32, which Docker Engine 25+ rejects.
		if (System.getProperty("api.version") == null && System.getenv("DOCKER_API_VERSION") == null) {
			System.setProperty("api.version", "1.41");
		}
	}

	private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:5.7")
			.withUrlParam("useSSL", "false");

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
					"spring.jpa.hibernate.ddl-auto=create-drop");
		}
	}
}
