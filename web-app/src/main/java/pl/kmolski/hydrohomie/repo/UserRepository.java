package pl.kmolski.hydrohomie.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.model.UserAccount;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserAccount, String> {}
