package pl.kmolski.hydrohomie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.model.Message.ConnectedMessage;

@Component
public class MqttRootTopicHandler implements MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttRootTopicHandler.class);

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        LOGGER.info("Received: " + message.getPayload());

        var mapper = new ObjectMapper();
        var connectedMessage = new ConnectedMessage("foo");

        try {
            LOGGER.info("Mapped message: " + mapper.writeValueAsString(connectedMessage));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
