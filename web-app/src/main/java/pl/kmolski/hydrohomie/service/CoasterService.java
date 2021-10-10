package pl.kmolski.hydrohomie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.model.Coaster;
import pl.kmolski.hydrohomie.repo.CoasterRepository;
import pl.kmolski.hydrohomie.repo.MeasurementRepository;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Component
@Transactional
public class CoasterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoasterService.class);

    private final CoasterRepository coasterRepository;
    private final MeasurementRepository measurementRepository;

    CoasterService(CoasterRepository coasterRepository,
                   MeasurementRepository measurementRepository) {
        this.coasterRepository = coasterRepository;
        this.measurementRepository = measurementRepository;
    }

    public Mono<Coaster> getCoasterEntity(String deviceName) {
        return coasterRepository.findById(deviceName)
                .switchIfEmpty(coasterRepository.create(deviceName));
    }

    public Mono<Tuple2<Coaster, Float>> getCoasterAndDailySumVolume(String deviceName) {
        return getCoasterEntity(deviceName)
                .zipWith(measurementRepository.findDailySumVolumeByDeviceName(deviceName, LocalDate.now()));
    }

    public Mono<? extends Coaster> updateCoasterInactivity(String deviceName, ZonedDateTime inactiveSince) {
        return getCoasterEntity(deviceName)
                .map(coaster -> coaster.setInactiveSince(inactiveSince))
                .flatMap(coasterRepository::save);
    }

    public Mono<Coaster> updateCoasterInitLoad(String deviceName, float initLoad) {
        return getCoasterEntity(deviceName)
                .map(coaster -> coaster.setInitLoad(initLoad).setInactiveSince(ZonedDateTime.now()))
                .flatMap(coasterRepository::save);
    }

    public Mono<Coaster> resetCoasterState(String deviceName) {
        return getCoasterEntity(deviceName)
                .map(coaster -> coaster.setInitLoad(null).setInactiveSince(null))
                .flatMap(coasterRepository::save);
    }
}
