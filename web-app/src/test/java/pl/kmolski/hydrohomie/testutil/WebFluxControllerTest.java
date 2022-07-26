package pl.kmolski.hydrohomie.testutil;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.kmolski.hydrohomie.webmvc.config.SecurityConfiguration;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@Import(SecurityConfiguration.class)
public abstract class WebFluxControllerTest {

    @Autowired
    private ApplicationContext context;

    protected WebTestClient webTestClient;

    @BeforeEach
    void setupWebTestClient() {
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
                .build();
    }
}
