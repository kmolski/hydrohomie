package pl.kmolski.hydrohomie.account.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;

@Data
@Table("user_data")
public class UserAccount implements Account {

    public static final String USER_ROLE = "ROLE_USER";

    @Id
    private final String username;
    private String password;
    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(USER_ROLE));
    }
}
