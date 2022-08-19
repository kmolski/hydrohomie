package pl.kmolski.hydrohomie.coaster.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoasterServiceImplTest {

    @Mock
    private CoasterRepository coasterRepository;

    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private CoasterServiceImpl coasterService;

    @Test
    void getExistingCoasterAndDailySumVolumeSucceeds() {
        var deviceName = "esp001";
        var tz = ZoneId.of("Etc/UTC");
        var date1 = LocalDate.ofEpochDay(0);
        var moment1 = Instant.ofEpochSecond(1);
        var sum = 10.0f;

        var coaster = new Coaster(deviceName).setTimezone(tz);
        when(coasterRepository.findById(deviceName)).thenReturn(Mono.just(coaster));
        when(coasterRepository.create(any(), any(), any())).thenReturn(Mono.empty());
        when(measurementRepository.findDailySumVolumeForCoaster(deviceName, date1, tz)).thenReturn(Mono.just(sum));

        var result = coasterService.getCoasterAndDailySumVolume(deviceName, moment1).block();
        assertEquals(coaster, result.getT1(), "Expected device not returned");
        assertEquals(sum, (double) result.getT2(), "Returned volume sum differs");
    }

    @Test
    void updateExistingCoasterInactivitySetsInactiveSinceTime() {
        var deviceName = "esp001";
        var tz = ZoneId.of("Etc/UTC");
        var inactiveSeconds = 1;
        var moment1 = Instant.ofEpochSecond(1);

        var coaster = new Coaster(deviceName).setTimezone(tz);
        when(coasterRepository.findById(deviceName)).thenReturn(Mono.just(coaster));
        when(coasterRepository.create(any(), any(), any())).thenReturn(Mono.empty());
        when(coasterRepository.save(coaster)).thenReturn(Mono.just(coaster));

        var result = coasterService.updateCoasterInactivity(deviceName, inactiveSeconds, moment1).block();
        assertEquals(deviceName, result.getDeviceName(), "Expected device not returned");
        assertEquals(Instant.EPOCH, result.getInactiveSince(), "Returned inactive since differs");
    }

    @Test
    void updateCoasterInitLoadSetsInitLoadAndInactiveSince() {
        var deviceName = "esp001";
        var tz = ZoneId.of("Etc/UTC");
        var initLoad = 42.0f;
        var moment1 = Instant.ofEpochSecond(1);

        var coaster = new Coaster(deviceName).setTimezone(tz);
        when(coasterRepository.findById(deviceName)).thenReturn(Mono.just(coaster));
        when(coasterRepository.create(any(), any(), any())).thenReturn(Mono.empty());
        when(coasterRepository.save(coaster)).thenReturn(Mono.just(coaster));

        var result = coasterService.updateCoasterInitLoad(deviceName, initLoad, moment1).block();
        assertEquals(deviceName, result.getDeviceName(), "Expected device not returned");
        assertEquals(initLoad, result.getInitLoad(), "Returned init load differs");
        assertEquals(moment1, result.getInactiveSince(), "Returned inactive since differs");
    }

    @Test
    void createMeasurementSavesTheMeasurementAndResetsInitLoad() {
        var deviceName = "esp001";
        var tz = ZoneId.of("Etc/UTC");
        var initLoad = 42.0f;
        var volume = 1701.0f;
        var moment1 = Instant.ofEpochSecond(1);

        var coaster = new Coaster(deviceName).setTimezone(tz).setInitLoad(initLoad);
        var measurement = new Measurement(null, deviceName, volume, Instant.EPOCH);
        when(coasterRepository.findById(deviceName)).thenReturn(Mono.just(coaster));
        when(coasterRepository.create(any(), any(), any())).thenReturn(Mono.empty());
        when(coasterRepository.save(coaster)).thenReturn(Mono.just(coaster));
        when(measurementRepository.save(measurement)).thenReturn(Mono.just(measurement));

        var result = coasterService.createMeasurement(deviceName, measurement, moment1).block();
        assertEquals(deviceName, result.getDeviceName(), "Expected device not returned");
        assertNull(result.getInitLoad(), "Returned init load should be null");
        assertEquals(moment1, result.getInactiveSince(), "Returned inactive since differs");
        verify(measurementRepository).save(measurement);
    }

    @Test
    void resetCoasterStateSetsInitLoadToNull() {
        var deviceName = "esp001";
        var tz = ZoneId.of("Etc/UTC");
        var initLoad = 42.0f;
        var moment1 = Instant.ofEpochSecond(1);

        var coaster = new Coaster(deviceName).setTimezone(tz).setInitLoad(initLoad);
        when(coasterRepository.findById(deviceName)).thenReturn(Mono.just(coaster));
        when(coasterRepository.create(any(), any(), any())).thenReturn(Mono.empty());
        when(coasterRepository.save(coaster)).thenReturn(Mono.just(coaster));

        var result = coasterService.resetCoasterState(deviceName, moment1).block();
        assertEquals(deviceName, result.getDeviceName(), "Expected device not returned");
        assertNull(result.getInitLoad(), "Returned init load should be null");
        assertEquals(moment1, result.getInactiveSince(), "Returned inactive since differs");
    }
}
