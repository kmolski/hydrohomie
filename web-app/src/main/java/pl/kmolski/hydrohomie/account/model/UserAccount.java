package pl.kmolski.hydrohomie.account.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.StringJoiner;

import static pl.kmolski.hydrohomie.account.model.AccountRole.ROLE_USER;

/**
 * Models the regular user account with username and hashed password.
 * Overrides toString() to prevent password hash leaks.
 */
@Data
@Table("user_data")
@AllArgsConstructor
public class UserAccount implements Account {

    @Id
    private final String username;
    private String password;
    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(ROLE_USER.authority());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserAccount.class.getSimpleName() + "(", ")")
                .add("username='" + username + "'")
                .add("enabled=" + enabled)
                .toString();
    }
}
