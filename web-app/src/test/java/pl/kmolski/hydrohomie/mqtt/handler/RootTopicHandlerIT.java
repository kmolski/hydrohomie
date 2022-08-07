package pl.kmolski.hydrohomie.mqtt.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
import pl.kmolski.hydrohomie.mqtt.model.CoasterMessage;
import pl.kmolski.hydrohomie.testutil.MqttHandlerIT;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RootTopicHandlerIT extends MqttHandlerIT {

    @Autowired
    private CoasterRepository coasterRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    @MockBean
    private Clock clock;

    @Test
    void handleCreatesNewDevice() throws MqttException, JsonProcessingException, InterruptedException {
        var mockDeviceId = "coaster000";
        assertNull(coasterRepository.findById(mockDeviceId).block(), "Mock device already exists");

        var moment1 = Instant.ofEpochSecond(1);
        when(clock.instant()).thenReturn(moment1);

        var expectedDevice = new Coaster(mockDeviceId).setInactiveSince(moment1).setTimezone(ZoneId.systemDefault());

        var connectedMessage = new CoasterMessage.ConnectedMessage(mockDeviceId);
        sendMqttMessage("hydrohomie", connectedMessage);

        assertMqttMessagesInOrder("hydrohomie/device/" + mockDeviceId, (topic, message) -> {
            var response = objectMapper.readValue(message.getPayload(), CoasterMessage.ListeningMessage.class);
            assertEquals(mockDeviceId, response.device(), "Device ID differs for ListeningMessage response");
            assertNull(response.initLoad(), "Initial load for coaster is not null");
            assertEquals(0.0f, response.initTotal(), "Initial total for coaster is not zero");
        });

        var actualDevice = coasterRepository.findById(mockDeviceId).block();
        assertEquals(expectedDevice, actualDevice, "Expected and actual coaster are not equal");
    }

    @Test
    void handleReturnsInitLoadAndTotal() throws MqttException, JsonProcessingException, InterruptedException {
        var moment1 = Instant.ofEpochSecond(1);
        var mockDeviceId = "coaster001";
        var mockDevice = coasterRepository.create(mockDeviceId, moment1, ZoneId.of("Etc/UTC")).block();
        assertNotNull(mockDevice, "Could not create mock coaster");

        var volumes = List.of(1.0f, 2.0f, 3.0f, 4.0f);
        var volumesSum = volumes.stream().mapToDouble(Float::doubleValue).sum();
        volumes.forEach(volume -> {
            var measurement = new Measurement(null, mockDeviceId, volume, moment1);
            measurementRepository.save(measurement).block();
        });
        coasterRepository.save(mockDevice.setInitLoad(5.0f)).block();

        when(clock.instant()).thenReturn(moment1);
        var connectedMessage = new CoasterMessage.ConnectedMessage(mockDeviceId);
        sendMqttMessage("hydrohomie", connectedMessage);

        assertMqttMessagesInOrder("hydrohomie/device/" + mockDeviceId, (topic, message) -> {
            var response = objectMapper.readValue(message.getPayload(), CoasterMessage.ListeningMessage.class);
            assertEquals(mockDeviceId, response.device(), "Device ID differs for ListeningMessage response");
            assertEquals(5.0f, response.initLoad(), "Initial load for coaster differs");
            assertEquals(volumesSum, response.initTotal(), "Initial total for coaster differs");
        });
    }
}
