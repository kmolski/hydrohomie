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

    Flux<Measurement> findAllByDeviceName(String deviceName);

    @Query("""
        select m.*
        from measurements m
        join coasters c on m.device_name = c.device_name
        where m.device_name = :device and c.owner = :owner
        order by m.timestamp desc limit 10""")
    Flux<Measurement> findTop10LatestByDeviceNameAndOwner(String deviceName, String owner);

    @Query("""
        select coalesce(sum(m.volume), 0) as volume, min(m.timestamp) as timestamp,
               date_trunc(:unit, m.timestamp at time zone :tz) as ts_group
        from measurements m
        join coasters c on m.device_name = c.device_name
        where m.device_name = :device and c.owner = :owner and m.timestamp between :start and :end
        group by ts_group""")
    Flux<Measurement> findByDeviceNameAndOwnerAndIntervalGrouped(String device, String owner, Instant start,
                                                                 Instant end, ChronoUnit unit, ZoneId tz);

    @Query("""
        select coalesce(sum(volume), 0) from measurements
        where device_name = :device and date(timestamp at time zone :tz) = :date""")
    Mono<Float> findDailySumVolumeForCoaster(String device, LocalDate date, ZoneId tz);
}
