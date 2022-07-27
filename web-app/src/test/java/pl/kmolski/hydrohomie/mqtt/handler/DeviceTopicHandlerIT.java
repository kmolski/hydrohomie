package pl.kmolski.hydrohomie.mqtt.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
import pl.kmolski.hydrohomie.mqtt.model.CoasterMessage;
import pl.kmolski.hydrohomie.testutil.MqttHandlerIT;
import reactor.util.retry.Retry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DeviceTopicHandlerIT extends MqttHandlerIT {

    @Autowired
    private CoasterRepository coasterRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    @MockBean
    private Clock clock;

    @Test
    void handleHeartbeatUpdatesInactiveTime() throws MqttException, JsonProcessingException {
        var moment1 = Instant.ofEpochSecond(1);
        var mockDeviceId = "coaster100";
        var mockDevice = coasterRepository.create(mockDeviceId, moment1, ZoneId.of("Etc/UTC")).block();
        assertNotNull(mockDevice, "Could not create mock coaster");

        var inactiveSeconds = 42;
        var moment2 = Instant.ofEpochSecond(2);
        when(clock.instant()).thenReturn(moment2);

        var heartbeat = new CoasterMessage.HeartbeatMessage(mockDeviceId, inactiveSeconds);
        sendMqttMessage("hydrohomie/device/" + mockDeviceId, heartbeat);

        coasterRepository.findById(mockDeviceId).map(coaster -> {
            assertEquals(moment2.minusSeconds(inactiveSeconds), coaster.getInactiveSince(), "Inactive since differs");
            return coaster;
        }).retryWhen(Retry.backoff(5, Duration.ofMillis(100))).block();
    }

    @Test
    void handleBeginUpdatesInitLoadAndInactiveTime() throws MqttException, JsonProcessingException {
        var moment1 = Instant.ofEpochSecond(1);
        var mockDeviceId = "coaster101";
        var mockDevice = coasterRepository.create(mockDeviceId, moment1, ZoneId.of("Etc/UTC")).block();
        assertNotNull(mockDevice, "Could not create mock coaster");

        var initLoad = 42.0f;
        var moment2 = Instant.ofEpochSecond(2);
        when(clock.instant()).thenReturn(moment2);

        var begin = new CoasterMessage.BeginMessage(mockDeviceId, initLoad);
        sendMqttMessage("hydrohomie/device/" + mockDeviceId, begin);

        coasterRepository.findById(mockDeviceId).map(coaster -> {
            assertEquals(initLoad, coaster.getInitLoad(), "Initial load differs");
            assertEquals(moment2, coaster.getInactiveSince(), "Inactive since differs");
            return coaster;
        }).retryWhen(Retry.backoff(5, Duration.ofMillis(100))).block();
    }

    @Test
    void handleEndInsertsMeasurementAndUpdatesInitLoad() throws MqttException, JsonProcessingException {
        var moment1 = Instant.ofEpochSecond(1);
        var mockDeviceId = "coaster102";
        var mockDevice = coasterRepository.create(mockDeviceId, moment1, ZoneId.of("Etc/UTC")).block();
        assertNotNull(mockDevice, "Could not create mock coaster");

        coasterRepository.findById(mockDeviceId).map(coaster -> coaster.setInitLoad(0.42f)).block();

        var moment2 = Instant.ofEpochSecond(2);
        when(clock.instant()).thenReturn(moment2);

        var volume = 42.0f;
        var end = new CoasterMessage.EndMessage(mockDeviceId, volume);
        sendMqttMessage("hydrohomie/device/" + mockDeviceId, end);

        coasterRepository.findById(mockDeviceId).map(coaster -> {
            assertNull(coaster.getInitLoad(), "Initial load is not null");
            assertEquals(moment2, coaster.getInactiveSince(), "Inactive since differs");
            return coaster;
        }).retryWhen(Retry.backoff(5, Duration.ofMillis(100))).block();

        var actual = measurementRepository
                .findByDeviceNameAndTimestampBetween(mockDeviceId, moment1, moment2)
                .collectList().block();
        assertEquals(1, actual.size(), "Unexpected measurements for coaster");
        assertNotNull(actual.get(0).id(), "Measurement ID is null");
        assertNotNull(actual.get(0).deviceName(), "Device name is null");
        assertEquals(moment2, actual.get(0).timestamp(), "Measurement timestamp differs");
        assertEquals(volume, actual.get(0).volume(), "Measurement volume differs");
    }

    @Test
    void handleDiscardResetsInitLoadAndInactiveSince() throws MqttException, JsonProcessingException {
        var moment1 = Instant.ofEpochSecond(1);
        var mockDeviceId = "coaster103";
        var mockDevice = coasterRepository.create(mockDeviceId, moment1, ZoneId.of("Etc/UTC")).block();
        assertNotNull(mockDevice, "Could not create mock coaster");

        coasterRepository.findById(mockDeviceId).map(coaster -> coaster.setInitLoad(0.42f)).block();

        var moment2 = Instant.ofEpochSecond(2);
        when(clock.instant()).thenReturn(moment2);

        var discard = new CoasterMessage.DiscardMessage(mockDeviceId);
        sendMqttMessage("hydrohomie/device/" + mockDeviceId, discard);

        coasterRepository.findById(mockDeviceId).map(coaster -> {
            assertNull(coaster.getInitLoad(), "Initial load is not null");
            assertEquals(moment2, coaster.getInactiveSince(), "Inactive since differs");
            return coaster;
        }).retryWhen(Retry.backoff(5, Duration.ofMillis(100))).block();
    }
}
