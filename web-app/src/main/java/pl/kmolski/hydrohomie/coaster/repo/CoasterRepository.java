package pl.kmolski.hydrohomie.coaster.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Repository of {@link Coaster}, used to store coaster details and device state.
 */
@Repository
public interface CoasterRepository extends ReactiveCrudRepository<Coaster, String> {

    /**
     * Fetch coaster entities that are not assigned to any user.
     *
     * @param pageable the page descriptor
     * @return {@link Flux} of {@link Coaster} entities
     */
    Flux<Coaster> findAllByOwnerIsNull(Pageable pageable);

    /**
     * Fetch the count of coasters that are not assigned to any user.
     *
     * @return the coaster count
     */
    Mono<Long> countByOwnerIsNull();

    /**
     * Fetch coaster entities assigned to the specified user.
     *
     * @param owner the owner's username
     * @param pageable the page descriptor
     * @return {@link Flux} of {@link Coaster} entities
     */
    Flux<Coaster> findAllByOwner(String owner, Pageable pageable);

    /**
     * Fetch the count of coasters assigned to the specified user.
     *
     * @param owner the owner's username
     * @return the coaster count
     */
    Mono<Long> countByOwner(String owner);

    /**
     * Fetch the coaster entity by device ID.
     *
     * @param deviceName the device ID
     * @param owner the coaster owner's username
     * @return the coaster entity
     */
    Mono<Coaster> findByDeviceNameAndOwner(String deviceName, String owner);

    /**
     * Create a coaster with the given device ID, inactivity time and timezone.
     *
     * @param deviceName the device ID
     * @param now the inactivity time
     * @param timezone the coaster's timezone
     * @return the new coaster entity
     * @param <S> the coaster type
     */
    @Query("""
        insert into coasters (device_name, inactive_since, timezone)
        values (:device_name, :now, :timezone) returning *""")
    <S extends Coaster> Mono<S> create(String deviceName, Instant now, ZoneId timezone);
}
