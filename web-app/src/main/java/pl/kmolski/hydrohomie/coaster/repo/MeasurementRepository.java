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

/**
 * Repository of {@link Measurement}, used to store coaster measurement data.
 */
@Repository
public interface MeasurementRepository extends ReactiveCrudRepository<Measurement, Integer> {

    /**
     * Fetch all measurements for the specified coaster.
     *
     * @param deviceName the device ID
     * @return {@link Flux} of {@link Measurement Measurements} for the coaster
     */
    Flux<Measurement> findAllByDeviceName(String deviceName);

    /**
     * Fetch the 10 latest measurements for the coaster.
     *
     * @param deviceName the device ID
     * @param owner the coaster owner's username
     * @return {@link Flux} of the latest {@link Measurement Measurements} for the coaster
     */
    @Query("""
        select m.*
        from measurements m
        join coasters c on m.device_name = c.device_name
        where m.device_name = :device and c.owner = :owner
        order by m.timestamp desc limit 10""")
    Flux<Measurement> findTop10LatestByDeviceNameAndOwner(String deviceName, String owner);

    /**
     * Fetch the measurements for the specified coaster and time period at time zone, grouped by the given time unit.
     *
     * @param device the device ID
     * @param owner the coaster owner's username
     * @param start the period start time
     * @param end the period end time
     * @param unit the time unit to group by
     * @param tz the period time zone
     * @return {@link Flux} of grouped {@link Measurement Measurements} for the coaster
     */
    @Query("""
        select coalesce(sum(m.volume), 0) as volume, min(m.timestamp) as timestamp,
               date_trunc(:unit, m.timestamp at time zone :tz) as ts_group
        from measurements m
        join coasters c on m.device_name = c.device_name
        where m.device_name = :device and c.owner = :owner and m.timestamp between :start and :end
        group by ts_group""")
    Flux<Measurement> findByDeviceNameAndOwnerAndIntervalGrouped(String device, String owner, Instant start,
                                                                 Instant end, ChronoUnit unit, ZoneId tz);

    /**
     * Fetch the sum of volume measurements for the given coaster and date.
     *
     * @param device the device ID
     * @param date the current date
     * @param tz the coaster time zone
     * @return the sum of volume measurements
     */
    @Query("""
        select coalesce(sum(volume), 0) from measurements
        where device_name = :device and date(timestamp at time zone :tz) = :date""")
    Mono<Float> findDailySumVolumeForCoaster(String device, LocalDate date, ZoneId tz);
}
