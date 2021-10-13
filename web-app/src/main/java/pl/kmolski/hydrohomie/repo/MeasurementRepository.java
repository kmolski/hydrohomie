package pl.kmolski.hydrohomie.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import pl.kmolski.hydrohomie.model.Measurement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public interface MeasurementRepository extends ReactiveCrudRepository<Measurement, Integer> {

    Flux<Measurement> findByDeviceNameAndTimestampBetween(String deviceName, Instant startTimestamp, Instant endTimestamp);

    @NonNull
    @Query("""
        select coalesce(sum(volume), 0) from measurements
        where device_name = :device and date(timestamp at time zone :tz) = :date""")
    Mono<Float> findDailySumVolumeByDeviceName(@NonNull String device, @NonNull LocalDate date, @NonNull ZoneId tz);
}
