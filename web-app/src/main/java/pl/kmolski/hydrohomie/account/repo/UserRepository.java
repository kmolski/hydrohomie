package pl.kmolski.hydrohomie.account.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserAccount, String> {

    Flux<UserAccount> findAllBy(Pageable pageable);

    Mono<UserDetails> findByUsername(String username);
}
