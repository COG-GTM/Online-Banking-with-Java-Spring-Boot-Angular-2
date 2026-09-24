package com.userFront;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Entry point for the UserFront smoke suite.
 *
 * <p>Boots the whole application against a MySQL 8.0 Testcontainer (the production major
 * version) with {@code ddl-auto=update}, so the schema is derived from the JPA entities and
 * no local MySQL is required. The container is started once per JVM and its JDBC coordinates
 * are pushed into the environment by {@link DatasourceInitializer} before the context
 * refreshes; {@code spring-boot-testcontainers} and {@code @ServiceConnection} only exist
 * from Boot 3.1 onwards.
 *
 * <p>Subclasses get an autoconfigured {@link org.springframework.test.web.servlet.MockMvc}
 * with the Spring Security filter chain applied.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = AbstractIntegrationTest.DatasourceInitializer.class)
public abstract class AbstractIntegrationTest {

    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    static {
        MYSQL.withDatabaseName("onlinebanking");
        // The Boot 1.5 stack ships MySQL Connector/J 5.1, which cannot speak caching_sha2_password.
        MYSQL.withCommand("--default-authentication-plugin=mysql_native_password");
        MYSQL.start();
    }

    public static class DatasourceInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            EnvironmentTestUtils.addEnvironment(applicationContext,
                    "spring.datasource.url=" + MYSQL.getJdbcUrl(),
                    "spring.datasource.username=" + MYSQL.getUsername(),
                    "spring.datasource.password=" + MYSQL.getPassword(),
                    "spring.datasource.driver-class-name=" + MYSQL.getDriverClassName(),
                    "spring.jpa.hibernate.ddl-auto=update",
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5Dialect");
        }
    }
}
