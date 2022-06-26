package pl.kmolski.hydrohomie.account.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserAccount, String> {

    Mono<UserDetails> findByUsername(String username);
}
