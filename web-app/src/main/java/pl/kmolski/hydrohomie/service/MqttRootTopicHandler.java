package pl.kmolski.hydrohomie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.config.MqttClientSettings;
import pl.kmolski.hydrohomie.model.Coaster;
import pl.kmolski.hydrohomie.model.CoasterMessage.ConnectedMessage;
import pl.kmolski.hydrohomie.model.CoasterMessage.ListeningMessage;
import pl.kmolski.hydrohomie.repo.CoasterRepository;
import pl.kmolski.hydrohomie.repo.MeasurementRepository;

import java.time.LocalDate;

import static pl.kmolski.hydrohomie.config.MqttConfiguration.DEVICE_TOPIC_SUFFIX;

@Component
public class MqttRootTopicHandler implements GenericHandler<ConnectedMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttRootTopicHandler.class);

    private final CoasterRepository coasterRepository;
    private final MeasurementRepository measurementRepository;
    private final MqttClientSettings mqttClientSettings;

    MqttRootTopicHandler(CoasterRepository coasterRepository,
                         MeasurementRepository measurementRepository,
                         MqttClientSettings mqttClientSettings) {
        this.coasterRepository = coasterRepository;
        this.measurementRepository = measurementRepository;
        this.mqttClientSettings = mqttClientSettings;
    }

    @Override
    public Object handle(ConnectedMessage message, MessageHeaders headers) throws MessagingException {
        LOGGER.debug("Received message on root topic: {}", message);

        var deviceName = message.device();
        return coasterRepository.findById(deviceName)
                .switchIfEmpty(coasterRepository.save(new Coaster(deviceName)))
                .then(measurementRepository.findDailySumVolumeByDeviceName(deviceName, LocalDate.now()))
                .map(initTotal -> new ListeningMessage(deviceName, initTotal))
                .map(response -> {
                    LOGGER.debug("Sending message to device topic: {}", message);
                    var responseTopic = mqttClientSettings.getTopic() + DEVICE_TOPIC_SUFFIX + deviceName;
                    return MessageBuilder.withPayload(response)
                            .copyHeaders(headers)
                            .setHeader(MqttHeaders.TOPIC, responseTopic)
                            .build();
                })
                .block();
    }
}
