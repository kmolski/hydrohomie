package pl.kmolski.hydrohomie.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import pl.kmolski.hydrohomie.model.Measurement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public interface MeasurementRepository extends ReactiveCrudRepository<Measurement, Integer> {

    Flux<Measurement> findByDeviceNameAndTimestampBetween(String deviceName, ZonedDateTime startTimestamp,
                                                          ZonedDateTime endTimestamp);

    @NonNull
    @Query("select coalesce(sum(volume), 0) from measurements where device_name = :device and date(timestamp) = :date")
    Mono<Float> findDailySumVolumeByDeviceName(@NonNull String device, @NonNull LocalDate date);
}
