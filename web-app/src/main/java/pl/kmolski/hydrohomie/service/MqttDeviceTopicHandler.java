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

import java.time.Clock;
import java.time.Instant;

@Component
public class MqttDeviceTopicHandler implements GenericHandler<DeviceTopicMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDeviceTopicHandler.class);

    private final CoasterService coasterService;
    private final MeasurementRepository measurementRepository;
    private final Clock clock;

    MqttDeviceTopicHandler(CoasterService coasterService, MeasurementRepository measurementRepository, Clock clock) {
        this.coasterService = coasterService;
        this.measurementRepository = measurementRepository;
        this.clock = clock;
    }

    @Override
    public Object handle(DeviceTopicMessage message, MessageHeaders headers) {
        LOGGER.info("Received on device topic '{}': {}", headers.get(MqttHeaders.RECEIVED_TOPIC), message);

        var deviceName = message.device();
        var now = Instant.now(clock);
        if (message instanceof HeartbeatMessage heartbeat) {
            coasterService.updateCoasterInactivity(deviceName, heartbeat.inactiveSeconds(), now).block();
        } else if (message instanceof BeginMessage begin) {
            coasterService.updateCoasterInitLoad(deviceName, begin.load(), now).block();
        } else if (message instanceof EndMessage end) {
            var measurement = new Measurement(null, message.device(), end.volume(), now);
            coasterService.resetCoasterState(deviceName, now).then(measurementRepository.save(measurement)).block();
        } else if (message instanceof DiscardMessage) {
            coasterService.resetCoasterState(deviceName, now).block();
        }

        return null;
    }
}
