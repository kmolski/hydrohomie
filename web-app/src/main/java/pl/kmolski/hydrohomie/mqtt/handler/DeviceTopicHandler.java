package pl.kmolski.hydrohomie.mqtt.handler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.coaster.service.CoasterService;
import pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.IncomingDeviceTopicMessage;

import java.time.Clock;
import java.time.Instant;

/**
 * MQTT message handler for {@link IncomingDeviceTopicMessage} on the device topic.
 * No responses are generated for the incoming messages.
 */
@Component
@RequiredArgsConstructor
public class DeviceTopicHandler implements GenericHandler<IncomingDeviceTopicMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceTopicHandler.class);

    private final CoasterService coasterService;
    private final Clock clock;

    @Override
    public Object handle(IncomingDeviceTopicMessage message, MessageHeaders headers) {
        LOGGER.info("Received on device topic '{}': {}", headers.get(MqttHeaders.RECEIVED_TOPIC), message);

        message.handle(coasterService, Instant.now(clock)).block();
        return null;
    }
}
