package pl.kmolski.hydrohomie.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pl.kmolski.hydrohomie.model.Coaster;

public interface CoasterRepository extends ReactiveCrudRepository<Coaster, String> {
}
