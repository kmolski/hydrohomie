package pl.kmolski.hydrohomie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.model.CoasterMessage.*;
import pl.kmolski.hydrohomie.model.Measurement;
import pl.kmolski.hydrohomie.repo.MeasurementRepository;

import java.time.Instant;

@Component
public class MqttDeviceTopicHandler implements GenericHandler<DeviceTopicMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDeviceTopicHandler.class);

    private final CoasterService coasterService;
    private final MeasurementRepository measurementRepository;

    MqttDeviceTopicHandler(CoasterService coasterService,
                           MeasurementRepository measurementRepository) {
        this.coasterService = coasterService;
        this.measurementRepository = measurementRepository;
    }

    @Override
    public Object handle(DeviceTopicMessage message, MessageHeaders headers) {
        LOGGER.info("Received message on device topic '{}': {}", headers.get(MqttHeaders.RECEIVED_TOPIC), message);

        var deviceName = message.device();
        if (message instanceof HeartbeatMessage heartbeat) {
            var inactiveSince = Instant.now().minusSeconds(heartbeat.inactiveSeconds());
            coasterService.updateCoasterInactivity(deviceName, inactiveSince).block();
        } else if (message instanceof BeginMessage begin) {
            coasterService.updateCoasterInitLoad(deviceName, begin.load(), Instant.now()).block();
        } else if (message instanceof EndMessage end) {
            var measurement = new Measurement(null, message.device(), end.volume(), Instant.now());
            coasterService.resetCoasterState(deviceName).then(measurementRepository.save(measurement)).block();
        } else if (message instanceof DiscardMessage) {
            coasterService.resetCoasterState(deviceName).block();
        }

        return null;
    }
}
