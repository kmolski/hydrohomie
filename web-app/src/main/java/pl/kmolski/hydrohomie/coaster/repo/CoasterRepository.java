package pl.kmolski.hydrohomie.coaster.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;

@Repository
public interface CoasterRepository extends ReactiveCrudRepository<Coaster, String> {

    Flux<Coaster> findAllByOwnerIsNull(Pageable pageable);

    Mono<Long> countByOwnerIsNull();

    Flux<Coaster> findAllByOwner(String owner, Pageable pageable);

    Mono<Long> countByOwner(String owner);

    Mono<Coaster> findByDeviceNameAndOwner(String deviceName, String owner);

    @Query("""
        insert into coasters (device_name, inactive_since, timezone)
        values (:device_name, :now, :timezone) returning *""")
    <S extends Coaster> Mono<S> create(String deviceName, Instant now, ZoneId timezone);
}
