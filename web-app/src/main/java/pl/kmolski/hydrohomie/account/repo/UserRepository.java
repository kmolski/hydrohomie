package pl.kmolski.hydrohomie.account.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository of {@link UserAccount}, used to store user account data.
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<UserAccount, String> {

    /**
     * Fetch the specified page of user account data.
     *
     * @param pageable the page descriptor
     * @return Flux of {@link UserAccount}
     */
    Flux<UserAccount> findAllBy(Pageable pageable);

    /**
     * Fetch the {@link UserDetails} instance for the given username.
     *
     * @param username the user's name
     * @return the specified user's details
     */
    Mono<UserDetails> findByUsername(String username);

    /**
     * Create a user with the given username, password hash and status.
     *
     * @param username the username
     * @param password the hashed password
     * @param enabled the account status
     * @return the new user's account
     * @param <S> the user account type
     */
    @NonNull
    @Query("""
        insert into user_data (username, password, enabled)
        values (:username, :password, :enabled) returning *""")
    <S extends UserAccount> Mono<S> create(@NonNull String username, @NonNull String password, boolean enabled);
}
