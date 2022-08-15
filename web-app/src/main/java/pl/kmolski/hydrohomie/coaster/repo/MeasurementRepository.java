package pl.kmolski.hydrohomie.coaster.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Repository
public interface MeasurementRepository extends ReactiveCrudRepository<Measurement, Integer> {

    Flux<Measurement> findByDeviceNameAndTimestampBetween(String deviceName, Instant start, Instant end);

    Flux<Measurement> findTop10ByDeviceNameOrderByTimestampDesc(String deviceName);

    @Query("""
        select coalesce(sum(volume), 0) as volume, min(timestamp) as timestamp,
               date_trunc(:unit, timestamp at time zone :tz) as ts_group
        from measurements
        where device_name = :device and timestamp between :start and :end
        group by ts_group""")
    Flux<Measurement> findByDeviceNameAndIntervalGrouped(String device, Instant start, Instant end,
                                                         ChronoUnit unit, ZoneId tz);

    @Query("""
        select coalesce(sum(volume), 0) from measurements
        where device_name = :device and date(timestamp at time zone :tz) = :date""")
    Mono<Float> findDailySumVolumeForCoaster(String device, LocalDate date, ZoneId tz);
}
