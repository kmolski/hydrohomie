package pl.kmolski.hydrohomie.coaster.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
import pl.kmolski.hydrohomie.webmvc.exception.EntityNotFoundException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.time.ZoneId;

@Service
@Transactional
@RequiredArgsConstructor
public class CoasterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoasterService.class);

    private final CoasterRepository coasterRepository;
    private final MeasurementRepository measurementRepository;

    public Mono<Coaster> findOrCreateCoasterEntity(String deviceName, Instant now) {
        return coasterRepository.findById(deviceName)
                .switchIfEmpty(coasterRepository.create(deviceName, now, ZoneId.systemDefault()));
    }

    public Mono<Tuple2<Coaster, Float>> getCoasterAndDailySumVolume(String deviceName, Instant now) {
        return findOrCreateCoasterEntity(deviceName, now)
                .zipWhen(coaster -> {
                    var today = now.atZone(coaster.getTimezone()).toLocalDate();
                    return measurementRepository.findDailySumVolumeForCoaster(deviceName, today, coaster.getTimezone());
                });
    }

    public Mono<Coaster> updateCoasterInactivity(String deviceName, int inactiveSeconds, Instant now) {
        var inactiveSince = now.minusSeconds(inactiveSeconds);

        return findOrCreateCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInactiveSince(inactiveSince))
                .flatMap(coasterRepository::save);
    }

    public Mono<Coaster> updateCoasterInitLoad(String deviceName, float initLoad, Instant now) {
        return findOrCreateCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInitLoad(initLoad).setInactiveSince(now))
                .flatMap(coasterRepository::save);
    }

    public Mono<Coaster> createMeasurement(String deviceName, Measurement measurement, Instant now) {
        return resetCoasterState(deviceName, now)
                .flatMap(coaster -> measurementRepository.save(measurement).thenReturn(coaster));
    }

    public Mono<Coaster> resetCoasterState(String deviceName, Instant now) {
        return findOrCreateCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInitLoad(null).setInactiveSince(now))
                .flatMap(coasterRepository::save);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Page<Coaster>> getUnassignedCoasters(Pageable pageable) {
        LOGGER.debug("Fetching unassigned coasters for page {}", pageable);
        return coasterRepository.findAllByOwnerIsNull(pageable).collectList()
                .zipWith(coasterRepository.countByOwnerIsNull())
                .doOnNext(page -> LOGGER.debug("Successfully fetched page {}", page))
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Coaster> assignCoasterToUser(String deviceName, String username) {
        return coasterRepository.findById(deviceName)
                .map(coaster -> coaster.setOwner(username))
                .flatMap(coasterRepository::save)
                .doOnNext(coaster -> LOGGER.info("Successfully assigned coaster '{}' to user={}",
                        coaster.getDeviceName(), username))
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Coaster not found")))
                .onErrorResume(
                        DataIntegrityViolationException.class,
                        exc -> Mono.error(new EntityNotFoundException("User not found")));
    }

    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    public Mono<Page<Coaster>> getUserAssignedCoasters(String username, Pageable pageable) {
        LOGGER.debug("Fetching assigned coasters for user '{}', page {}", username, pageable);
        return coasterRepository.findAllByOwner(username, pageable).collectList()
                .zipWith(coasterRepository.countByOwner(username))
                .doOnNext(page -> LOGGER.debug("Successfully fetched page {}", page))
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }
}
