package pl.kmolski.hydrohomie.webmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import pl.kmolski.hydrohomie.account.model.AccountRole;

/**
 * Global web security and password encoding configuration.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    /**
     * Configure the Argon2id password encoder, as recommended by the
     * <a href="https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html">OWASP Cheat Sheet</a>
     *
     * @return password encoder using the Argon2id algorithm
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder();
    }

    /**
     * Configure the Spring {@link SecurityWebFilterChain}.
     * @param http the web security configurator
     * @return the configured {@link SecurityWebFilterChain} instance
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/login", "/webjars/**").permitAll()
                .pathMatchers("/admin/**").hasAuthority(AccountRole.ROLE_ADMIN.name())
                .pathMatchers("/user/**").hasAuthority(AccountRole.ROLE_USER.name())
                .pathMatchers("/**").authenticated()
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/"))
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(new RedirectServerLogoutSuccessHandler())
                    .and()
                .build();
    }
}
