package pl.kmolski.hydrohomie.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import pl.kmolski.hydrohomie.model.Coaster;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;

public interface CoasterRepository extends ReactiveCrudRepository<Coaster, String> {
    @NonNull
    @Query("insert into coasters(device_name, inactive_since, timezone) values (:device_name, :now, :timezone)")
    <S extends Coaster> Mono<S> create(@NonNull String deviceName, @NonNull Instant now, @NonNull ZoneId timezone);
}
