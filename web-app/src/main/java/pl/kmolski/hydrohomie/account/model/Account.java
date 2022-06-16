package pl.kmolski.hydrohomie.account.model;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface Account {

    String getUsername();
    String getPasswordHash();

    boolean isEnabled();

    Collection<? extends GrantedAuthority> getAuthorities();
}
