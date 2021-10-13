package pl.kmolski.hydrohomie.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import pl.kmolski.hydrohomie.model.Coaster;
import reactor.core.publisher.Mono;

import java.time.ZoneId;

public interface CoasterRepository extends ReactiveCrudRepository<Coaster, String> {
    @NonNull
    @Query("insert into coasters(device_name, timezone) values (:device_name, :timezone)")
    <S extends Coaster> Mono<S> create(@NonNull String deviceName, @NonNull ZoneId timezone);
}
