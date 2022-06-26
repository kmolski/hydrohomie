package pl.kmolski.hydrohomie.account.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.account.config.AdminAccountSettings;
import pl.kmolski.hydrohomie.account.model.Account;

import java.util.Collection;
import java.util.Set;

@Component
public class AdminAccount implements Account {

    public static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final String username;
    private final String passwordHash;

    AdminAccount(AdminAccountSettings adminAccountSettings, PasswordEncoder passwordEncoder) {
        this.username = adminAccountSettings.username();
        this.passwordHash = passwordEncoder.encode(adminAccountSettings.password());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(ADMIN_ROLE));
    }
}
