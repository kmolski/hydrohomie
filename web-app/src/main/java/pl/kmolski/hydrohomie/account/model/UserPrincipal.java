package pl.kmolski.hydrohomie.account.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * <p>Implementation of {@link UserDetails} based on the common {@link Account} interface.</p>
 * <p>Account expiration, account locking and credential expiration are not implemented.</p>
 */
public record UserPrincipal(Account account) implements UserDetails {

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public String getPassword() {
        return account.getPasswordHash();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return account.getAuthorities();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return account.isEnabled();
    }
}
