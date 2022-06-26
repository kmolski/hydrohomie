package pl.kmolski.hydrohomie.account.model;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * <p>Common interface for password-protected user and admin accounts.</p>
 * <p>
 *     Support for account expiration, account locking and credential expiration is not required.
 *     Can be implemented by database-stored accounts and Spring services.
 * </p>
 */
public interface Account extends UserDetails {

    @Override
    default boolean isAccountNonExpired() {
        return true;
    }

    @Override
    default boolean isAccountNonLocked() {
        return true;
    }

    @Override
    default boolean isCredentialsNonExpired() {
        return true;
    }
}
