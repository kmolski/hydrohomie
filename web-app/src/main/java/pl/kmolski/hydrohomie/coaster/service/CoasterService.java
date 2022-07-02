package pl.kmolski.hydrohomie.coaster.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
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

    public Mono<Coaster> getCoasterEntity(String deviceName, Instant now) {
        return coasterRepository.findById(deviceName)
                .switchIfEmpty(coasterRepository.create(deviceName, now, ZoneId.systemDefault()));
    }

    public Mono<Tuple2<Coaster, Float>> getCoasterAndDailySumVolume(String deviceName, Instant now) {
        return getCoasterEntity(deviceName, now)
                .zipWhen(coaster -> {
                    var today = now.atZone(coaster.getTimezone()).toLocalDate();
                    return measurementRepository.findDailySumVolumeForCoaster(deviceName, today, coaster.getTimezone());
                });
    }

    public Mono<Coaster> updateCoasterInactivity(String deviceName, int inactiveSeconds, Instant now) {
        var inactiveSince = now.minusSeconds(inactiveSeconds);

        return getCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInactiveSince(inactiveSince))
                .flatMap(coasterRepository::save);
    }

    public Mono<Coaster> updateCoasterInitLoad(String deviceName, float initLoad, Instant now) {
        return getCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInitLoad(initLoad).setInactiveSince(now))
                .flatMap(coasterRepository::save);
    }

    public Mono<Coaster> createMeasurement(String deviceName, Measurement measurement, Instant now) {
        return resetCoasterState(deviceName, now)
                .flatMap(coaster -> measurementRepository.save(measurement).thenReturn(coaster));
    }

    public Mono<Coaster> resetCoasterState(String deviceName, Instant now) {
        return getCoasterEntity(deviceName, now)
                .map(coaster -> coaster.setInitLoad(null))
                .flatMap(coasterRepository::save);
    }
}
