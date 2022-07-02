package pl.kmolski.hydrohomie.mqtt.handler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.mqtt.config.MqttClientSettings;
import pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.ConnectedMessage;
import pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.ListeningMessage;
import pl.kmolski.hydrohomie.coaster.service.CoasterService;

import java.time.Clock;
import java.time.Instant;

import static pl.kmolski.hydrohomie.mqtt.config.MqttConfiguration.DEVICE_SUBTOPIC;

@Component
@RequiredArgsConstructor
public class RootTopicHandler implements GenericHandler<ConnectedMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootTopicHandler.class);

    private final CoasterService coasterService;
    private final MqttClientSettings mqttClientSettings;
    private final Clock clock;

    @Override
    public Message<ListeningMessage> handle(ConnectedMessage message, MessageHeaders headers) {
        LOGGER.info("Received on root topic '{}': {}", headers.get(MqttHeaders.RECEIVED_TOPIC), message);

        return coasterService.getCoasterAndDailySumVolume(message.device(), Instant.now(clock))
                .map(coasterAndTotal -> {
                    var coaster = coasterAndTotal.getT1();
                    float initTotal = coasterAndTotal.getT2();
                    return new ListeningMessage(coaster.getDeviceName(), coaster.getInitLoad(), initTotal);
                })
                .map(response -> {
                    var responseTopic = mqttClientSettings.topic() + DEVICE_SUBTOPIC + response.device();
                    LOGGER.info("Sending to device topic '{}': {}", responseTopic, response);

                    return MessageBuilder.withPayload(response)
                            .copyHeaders(headers)
                            .setHeader(MqttHeaders.TOPIC, responseTopic)
                            .build();
                })
                .block();
    }
}
