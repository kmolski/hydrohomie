package pl.kmolski.hydrohomie.testutil;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class WebFluxControllerIT extends WebFluxControllerTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14.2");

    static String getR2dbcUrl() {
        return String.format(
                "r2dbc:postgresql://%s:%s/%s",
                WebFluxControllerIT.POSTGRES.getHost(),
                WebFluxControllerIT.POSTGRES.getMappedPort(5432),
                WebFluxControllerIT.POSTGRES.getDatabaseName()
        );
    }

    @DynamicPropertySource
    static void setupPostgresContainer(DynamicPropertyRegistry propertyRegistry) {
        POSTGRES.start();

        propertyRegistry.add("spring.liquibase.url", POSTGRES::getJdbcUrl);
        propertyRegistry.add("spring.liquibase.user", POSTGRES::getUsername);
        propertyRegistry.add("spring.liquibase.password", POSTGRES::getPassword);

        propertyRegistry.add("spring.r2dbc.url", WebFluxControllerIT::getR2dbcUrl);
        propertyRegistry.add("spring.r2dbc.username", POSTGRES::getUsername);
        propertyRegistry.add("spring.r2dbc.password", POSTGRES::getPassword);
    }
}
