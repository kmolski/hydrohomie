package pl.kmolski.hydrohomie.account.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.kmolski.hydrohomie.account.service.AdminAccount;

/**
 * User roles for admin and user accounts.
 */
public enum AccountRole {
    /**
     * Role possessed by all {@link UserAccount UserAccounts}.
     */
    ROLE_USER,
    /**
     * Role possessed by the {@link AdminAccount}.
     */
    ROLE_ADMIN;

    /**
     * Return the {@link GrantedAuthority} for a role
     *
     * @return the {@link SimpleGrantedAuthority} object for the role
     */
    public GrantedAuthority authority() {
        return new SimpleGrantedAuthority(name());
    }
}
