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

import static pl.kmolski.hydrohomie.account.service.AdminAccount.ADMIN_ROLE;
import static pl.kmolski.hydrohomie.account.model.UserAccount.USER_ROLE;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/login", "/webjars/**").permitAll()
                .pathMatchers("/admin/**").hasAuthority(ADMIN_ROLE)
                .pathMatchers("/user/**").hasAuthority(USER_ROLE)
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
