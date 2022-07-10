package pl.kmolski.hydrohomie.account.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

import static pl.kmolski.hydrohomie.account.model.AccountRole.ROLE_USER;

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
}
