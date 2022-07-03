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

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserAccount, String> {

    Flux<UserAccount> findAllBy(Pageable pageable);

    Mono<UserDetails> findByUsername(String username);

    @NonNull
    @Query("""
        insert into user_data (username, password, enabled)
        values (:username, :password, :enabled) returning *""")
    <S extends UserAccount> Mono<S> create(@NonNull String username, @NonNull String password, boolean enabled);
}
