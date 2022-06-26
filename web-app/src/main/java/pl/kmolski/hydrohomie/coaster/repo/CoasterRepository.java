package pl.kmolski.hydrohomie.coaster.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;

@Repository
public interface CoasterRepository extends ReactiveCrudRepository<Coaster, String> {

    @NonNull
    @Query("insert into coasters(device_name, inactive_since, timezone) values (:device_name, :now, :timezone)")
    <S extends Coaster> Mono<S> create(@NonNull String deviceName, @NonNull Instant now, @NonNull ZoneId timezone);
}