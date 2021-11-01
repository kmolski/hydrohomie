package pl.kmolski.hydrohomie.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.config.AdminAccountSettings;
import pl.kmolski.hydrohomie.model.Account;

import java.util.Collection;
import java.util.Set;

@Component
public class AdminAccount implements Account {

    private static final String ADMIN_ROLE = "ADMIN";

    AdminAccount(AdminAccountSettings adminAccountSettings, PasswordEncoder passwordEncoder) {
        this.username = adminAccountSettings.getUsername();
        this.passwordHash = passwordEncoder.encode(adminAccountSettings.getPassword());
    }

    private final String username;
    private final String passwordHash;

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
