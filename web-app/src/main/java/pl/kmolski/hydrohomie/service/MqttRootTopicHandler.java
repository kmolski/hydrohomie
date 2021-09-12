package pl.kmolski.hydrohomie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.model.Coaster;
import pl.kmolski.hydrohomie.model.CoasterMessage.ConnectedMessage;
import pl.kmolski.hydrohomie.repo.CoasterRepository;

@Component
public class MqttRootTopicHandler implements GenericHandler<ConnectedMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttRootTopicHandler.class);

    private final CoasterRepository coasterRepository;

    MqttRootTopicHandler(CoasterRepository coasterRepository) {
        this.coasterRepository = coasterRepository;
    }

    @Override
    public Object handle(ConnectedMessage message, MessageHeaders headers) throws MessagingException {
        LOGGER.debug("Received message on root topic: {}", message);

        var deviceName = message.device();
        coasterRepository.findById(deviceName)
                         .switchIfEmpty(coasterRepository.save(new Coaster(deviceName)))
                         .block();

        return null;
    }
}
