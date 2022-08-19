package pl.kmolski.hydrohomie.coaster.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Coaster state management service. Operations like inactivity time/initial load
 * update, coaster state reset, creating & fetching measurements are handled here.
 */
@Service
@RequiredArgsConstructor
public class CoasterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoasterService.class);

    private final CoasterRepository coasterRepository;
    private final MeasurementRepository measurementRepository;

    private Mono<Coaster> findOrCreateCoasterEntity(String deviceName, Instant now) {
        LOGGER.debug("Fetching coaster '{}'", deviceName);
        return coasterRepository.findById(deviceName)
                .switchIfEmpty(coasterRepository.create(deviceName, now, ZoneId.systemDefault()));
    }

    /**
     * Fetch the coaster entity and the sum of volume measurements for the given date.
     *
     * @param deviceName the device ID
     * @param now the date to fetch the volume sum for
     * @return {@link Tuple2} containing the {@link Coaster} entity and volume sum
     */
    @Transactional(readOnly = true)
    public Mono<Tuple2<Coaster, Float>> getCoasterAndDailySumVolume(String deviceName, Instant now) {
        return findOrCreateCoasterEntity(deviceName, now)
                .zipWhen(coaster -> {
                    var today = now.atZone(coaster.getTimezone()).toLocalDate();
                    LOGGER.debug("Fetching daily sum volume for coaster '{}'", deviceName);
                    return measurementRepository.findDailySumVolumeForCoaster(deviceName, today, coaster.getTimezone());
                });
    }

    /**
     * Update the coaster inactivity time with the specified value.
     *
     * @param deviceName the device ID
     * @param inactiveSeconds the amount of seconds for which the coaster was inactive
     * @param now the current time
     * @return the updated {@link Coaster} entity
     */
    @Transactional
    public Mono<Coaster> updateCoasterInactivity(String deviceName, int inactiveSeconds, Instant now) {
        var inactiveSince = now.minusSeconds(inactiveSeconds);
        LOGGER.info("Updating coaster '{}' with inactiveSince={}", deviceName, inactiveSince);
        return findOrCreateCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInactiveSince(inactiveSince))
                .flatMap(coasterRepository::save);
    }

    /**
     * Update the coaster initial weight measurement with the specified value.
     *
     * @param deviceName the device ID
     * @param initLoad the initial weight measurement
     * @param now the current time
     * @return the updated {@link Coaster} entity
     */
    @Transactional
    public Mono<Coaster> updateCoasterInitLoad(String deviceName, float initLoad, Instant now) {
        LOGGER.info("Updating coaster '{}' with initLoad={}, inactiveSince={}", deviceName, initLoad, now);
        return findOrCreateCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInitLoad(initLoad).setInactiveSince(now))
                .flatMap(coasterRepository::save);
    }

    /**
     * Create a {@link Measurement} for the specified {@link Coaster}.
     *
     * @param deviceName the device ID
     * @param measurement the measurement record
     * @param now the current time
     * @return the {@link Coaster entity}
     */
    @Transactional
    public Mono<Coaster> createMeasurement(String deviceName, Measurement measurement, Instant now) {
        LOGGER.info("Creating measurement {} for coaster '{}'", measurement, deviceName);
        return resetCoasterState(deviceName, now)
                .flatMap(coaster -> measurementRepository.save(measurement).thenReturn(coaster));
    }

    /**
     * Reset the coaster state in the database.
     *
     * @param deviceName the device ID
     * @param now the current time
     * @return the reset {@link Coaster} entity
     */
    @Transactional
    public Mono<Coaster> resetCoasterState(String deviceName, Instant now) {
        LOGGER.info("Resetting state for coaster '{}'", deviceName);
        return findOrCreateCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInitLoad(null).setInactiveSince(now))
                .flatMap(coasterRepository::save);
    }

    /**
     * Fetch the measurements for the specified coaster and time period at time zone, grouped by the given time unit.
     *
     * @param deviceName the device ID
     * @param username the coaster owner's username
     * @param start the period start time
     * @param end the period end time
     * @param timeUnit the time unit to group by
     * @param timezone the period time zone
     * @return {@link Flux} of grouped {@link Measurement Measurements} for the coaster
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    public Flux<Measurement> getMeasurementsByIntervalGrouped(String deviceName, String username, Instant start,
                                                              Instant end, ChronoUnit timeUnit, ZoneId timezone) {
        LOGGER.debug("Fetching grouped measurements for coaster '{}'", deviceName);
        return measurementRepository.findByDeviceNameAndOwnerAndIntervalGrouped(deviceName, username, start, end, timeUnit, timezone);
    }

    /**
     * Fetch the 10 latest measurements for the coaster.
     *
     * @param deviceName the device ID
     * @param username the coaster owner's username
     * @return {@link Flux} of the latest {@link Measurement Measurements} for the coaster
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    public Flux<Measurement> getLatestMeasurements(String deviceName, String username) {
        LOGGER.debug("Fetching latest measurements for coaster '{}'", deviceName);
        return measurementRepository.findTop10LatestByDeviceNameAndOwner(deviceName, username);
    }
}
