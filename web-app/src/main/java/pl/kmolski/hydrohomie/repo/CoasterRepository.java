package pl.kmolski.hydrohomie.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import pl.kmolski.hydrohomie.model.Coaster;
import reactor.core.publisher.Mono;

public interface CoasterRepository extends ReactiveCrudRepository<Coaster, String> {
    @NonNull
    @Query("insert into coasters(device_name) values (:device_name)")
    <S extends Coaster> Mono<S> create(@NonNull String deviceName);
}
