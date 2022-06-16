package pl.kmolski.hydrohomie.account.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.account.model.UserAccount;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserAccount, String> {}
