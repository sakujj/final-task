package io.github.sakujj.nms.testcontainer;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test")
public abstract class CommonPostgresContainerInitializer {

    private static final PostgreSQLContainer<?> postgres;

    private static final String DOCKER_IMAGE = "postgres:16.2-alpine3.19";
    private static final String DB_NAME = "test_db";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "postgres";

    static {
        postgres = new PostgreSQLContainer<>(DOCKER_IMAGE)
                .withDatabaseName(DB_NAME)
                .withUsername(DB_USERNAME)
                .withPassword(DB_PASSWORD);
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
