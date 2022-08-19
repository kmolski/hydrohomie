package pl.kmolski.hydrohomie.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.account.dto.PlaintextPassword;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import reactor.core.publisher.Mono;

/**
 * User account management service. Operations like user creation, removal,
 * password changes, account enable/disable & user list fetch are handled here.
 */
public interface UserService extends ReactiveUserDetailsService {

    @Override
    @Transactional(readOnly = true)
    Mono<UserDetails> findByUsername(String username);

    /**
     * Create the user account with the specified username, plaintext password and enabled status.
     *
     * @param username the username of the account
     * @param password the plaintext password
     * @param enabled  the enabled status
     * @return the new user's account
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Mono<UserAccount> createAccount(String username, PlaintextPassword password, boolean enabled);

    /**
     * Fetch the specified page of user account data.
     *
     * @param pageable the page descriptor
     * @return the requested page of {@link UserAccount}
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Mono<Page<UserAccount>> getAllAccounts(Pageable pageable);

    /**
     * Change the user's existing password to the specified plaintext password.
     *
     * @param username the name of the user to update
     * @param password the new plaintext password
     * @return the updated {@link UserAccount}
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and principal.username == username)")
    Mono<UserAccount> updatePassword(String username, PlaintextPassword password);

    /**
     * Set the user's enabled status.
     *
     * @param username the name of the user to update
     * @param enabled  the desired enabled status
     * @return the updated {@link UserAccount}
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Mono<UserAccount> setAccountEnabled(String username, boolean enabled);

    /**
     * Delete the specified user.
     *
     * @param username the name of the user to delete
     * @return the deleted user
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Mono<UserAccount> deleteAccount(String username);
}
