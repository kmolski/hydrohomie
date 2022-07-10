package pl.kmolski.hydrohomie.account.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum AccountRole {
    ROLE_USER,
    ROLE_ADMIN;

    public GrantedAuthority authority() {
        return new SimpleGrantedAuthority(name());
    }
}
