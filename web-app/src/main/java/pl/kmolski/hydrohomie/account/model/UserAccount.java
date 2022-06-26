package pl.kmolski.hydrohomie.account.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;

@Table("user_data")
public class UserAccount implements Account {

    public static final String USER_ROLE = "ROLE_USER";

    @Id
    private final String username;
    private String passwordHash;
    private boolean enabled;

    public UserAccount(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    public UserAccount setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public UserAccount setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(USER_ROLE));
    }
}
