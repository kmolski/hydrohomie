package pl.kmolski.hydrohomie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.config.MqttClientSettings;
import pl.kmolski.hydrohomie.model.CoasterMessage.ConnectedMessage;
import pl.kmolski.hydrohomie.model.CoasterMessage.ListeningMessage;

import java.time.Clock;
import java.time.Instant;

import static pl.kmolski.hydrohomie.config.MqttConfiguration.DEVICE_TOPIC_SUFFIX;

@Component
public class MqttRootTopicHandler implements GenericHandler<ConnectedMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttRootTopicHandler.class);

    private final CoasterService coasterService;
    private final MqttClientSettings mqttClientSettings;
    private final Clock clock;

    MqttRootTopicHandler(CoasterService coasterService, MqttClientSettings mqttClientSettings, Clock clock) {
        this.coasterService = coasterService;
        this.mqttClientSettings = mqttClientSettings;
        this.clock = clock;
    }

    @Override
    public Message<ListeningMessage> handle(ConnectedMessage message, MessageHeaders headers) {
        LOGGER.info("Received on root topic '{}': {}", headers.get(MqttHeaders.RECEIVED_TOPIC), message);

        var deviceName = message.device();
        var now = Instant.now(clock);
        return coasterService.getCoasterAndDailySumVolume(deviceName, now)
                .map(coasterAndTotal -> {
                    var coaster = coasterAndTotal.getT1();
                    var initTotal = coasterAndTotal.getT2();
                    return new ListeningMessage(coaster.getDeviceName(), coaster.getInitLoad(), initTotal);
                })
                .map(response -> {
                    var responseTopic = mqttClientSettings.getTopic() + DEVICE_TOPIC_SUFFIX + deviceName;
                    LOGGER.info("Sending to device topic '{}': {}", responseTopic, response);

                    return MessageBuilder.withPayload(response)
                            .copyHeaders(headers)
                            .setHeader(MqttHeaders.TOPIC, responseTopic)
                            .build();
                })
                .block();
    }
}
