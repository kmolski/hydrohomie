package pl.kmolski.hydrohomie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.model.CoasterMessage.DeviceTopicMessage;

@Component
public class MqttDeviceTopicHandler implements GenericHandler<DeviceTopicMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttRootTopicHandler.class);

    @Override
    public Object handle(DeviceTopicMessage payload, MessageHeaders headers) {
        LOGGER.debug("Received message: {}, headers: {}", payload, headers);
        return null;
    }
}
