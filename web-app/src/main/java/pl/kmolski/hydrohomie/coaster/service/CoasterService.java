package pl.kmolski.hydrohomie.coaster.service;

import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Coaster state management service. Operations like inactivity time/initial load
 * update, coaster state reset, creating and fetching measurements are handled here.
 */
public interface CoasterService {

    /**
     * Fetch the coaster entity and the sum of volume measurements for the given date.
     *
     * @param deviceName the device ID
     * @param now        the date to fetch the volume sum for
     * @return {@link Tuple2} containing the {@link Coaster} entity and volume sum
     */
    Mono<Tuple2<Coaster, Float>> getCoasterAndDailySumVolume(String deviceName, Instant now);

    /**
     * Update the coaster inactivity time with the specified value.
     *
     * @param deviceName      the device ID
     * @param inactiveSeconds the amount of seconds for which the coaster was inactive
     * @param now             the current time
     * @return the updated {@link Coaster} entity
     */
    Mono<Coaster> updateCoasterInactivity(String deviceName, int inactiveSeconds, Instant now);

    /**
     * Update the coaster initial weight measurement with the specified value.
     *
     * @param deviceName the device ID
     * @param initLoad   the initial weight measurement
     * @param now        the current time
     * @return the updated {@link Coaster} entity
     */
    Mono<Coaster> updateCoasterInitLoad(String deviceName, float initLoad, Instant now);

    /**
     * Create a {@link Measurement} for the specified {@link Coaster}.
     *
     * @param deviceName  the device ID
     * @param measurement the measurement record
     * @param now         the current time
     * @return the {@link Coaster entity}
     */
    Mono<Coaster> createMeasurement(String deviceName, Measurement measurement, Instant now);

    /**
     * Reset the coaster state in the database.
     *
     * @param deviceName the device ID
     * @param now        the current time
     * @return the reset {@link Coaster} entity
     */
    Mono<Coaster> resetCoasterState(String deviceName, Instant now);

    /**
     * Fetch the measurements for the specified coaster and time period at time zone, grouped by the given time unit.
     *
     * @param deviceName the device ID
     * @param username   the coaster owner's username
     * @param start      the period start time
     * @param end        the period end time
     * @param timeUnit   the time unit to group by
     * @param timezone   the period time zone
     * @return {@link Flux} of grouped {@link Measurement Measurements} for the coaster
     */
    Flux<Measurement> getMeasurementsByIntervalGrouped(String deviceName, String username, Instant start,
                                                       Instant end, ChronoUnit timeUnit, ZoneId timezone);

    /**
     * Fetch the 10 latest measurements for the coaster.
     *
     * @param deviceName the device ID
     * @param username   the coaster owner's username
     * @return {@link Flux} of the latest {@link Measurement Measurements} for the coaster
     */
    Flux<Measurement> getLatestMeasurements(String deviceName, String username);
}
