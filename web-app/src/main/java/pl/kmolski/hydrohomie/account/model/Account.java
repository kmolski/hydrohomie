package pl.kmolski.hydrohomie.account.model;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Common interface for password-protected user and admin accounts.
 * Can be implemented by database-stored accounts and Spring services.
 */
public interface Account {

    /**
     * Return the username of the account
     * @return the non-null username
     */
    String getUsername();

    /**
     * Return the password hash of the account
     * @return the password hash
     */
    String getPasswordHash();

    /**
     * Return true if the account is enabled
     * @return {@code true} if the account is enabled
     */
    boolean isEnabled();

    /**
     * Return a collection of authorities granted to the user
     * @return collection of authorities
     */
    Collection<? extends GrantedAuthority> getAuthorities();
}
