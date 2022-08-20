package pl.kmolski.hydrohomie.account.service;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.account.config.AdminAccountSettings;
import pl.kmolski.hydrohomie.account.model.Account;

import java.util.Collection;
import java.util.Set;

import static pl.kmolski.hydrohomie.account.model.AccountRole.ROLE_ADMIN;

/**
 * <p>Admin user account, with credentials provided through Spring properties.
 * <p>
 * This class uses the following properties:
 * <ul>
 *     <li>admin.username - admin username</li>
 *     <li>admin.password - admin plaintext password</li>
 * </ul>
 */
@Getter
@Component
public class AdminAccount implements Account {

    private final String username;
    private final String password;

    AdminAccount(AdminAccountSettings adminAccountSettings, PasswordEncoder passwordEncoder) {
        this.username = adminAccountSettings.username();
        this.password = passwordEncoder.encode(adminAccountSettings.password());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(ROLE_ADMIN.authority());
    }
}
