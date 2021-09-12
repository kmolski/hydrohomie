package pl.kmolski.hydrohomie.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pl.kmolski.hydrohomie.model.Measurement;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface MeasurementRepository extends ReactiveCrudRepository<Measurement, Integer> {

    Flux<Measurement> findByDeviceNameAndTimestampBetween(String deviceName, LocalDateTime startTimestamp,
                                                          LocalDateTime endTimestamp);
}
