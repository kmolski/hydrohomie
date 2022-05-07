package pl.kmolski.hydrohomie.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.model.CoasterMessage.*;
import pl.kmolski.hydrohomie.service.CoasterService;

import java.time.Clock;
import java.time.Instant;

@Component
public class MqttDeviceTopicHandler implements GenericHandler<IncomingDeviceTopicMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDeviceTopicHandler.class);

    private final CoasterService coasterService;
    private final Clock clock;

    MqttDeviceTopicHandler(CoasterService coasterService, Clock clock) {
        this.coasterService = coasterService;
        this.clock = clock;
    }

    @Override
    public Object handle(IncomingDeviceTopicMessage message, MessageHeaders headers) {
        LOGGER.info("Received on device topic '{}': {}", headers.get(MqttHeaders.RECEIVED_TOPIC), message);

        message.handle(coasterService, Instant.now(clock)).block();
        return null;
    }
}
