package pl.kmolski.hydrohomie.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import pl.kmolski.hydrohomie.mqtt.handler.DeviceTopicHandler;
import pl.kmolski.hydrohomie.mqtt.handler.RootTopicHandler;
import pl.kmolski.hydrohomie.mqtt.model.CoasterMessage;
import pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.ConnectedMessage;
import pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.IncomingDeviceTopicMessage;


/**
 * Bean configuration for inbound and outbound MQTT clients.
 */
@Configuration
public class MqttConfiguration {

    /**
     * MQTT subtopic for individual devices
     */
    public static final String DEVICE_SUBTOPIC = "/device/";

    /**
     * Configure the {@link MqttPahoClientFactory} instance.
     * @param settings the MQTT client connection settings
     * @return the client factory
     */
    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory(MqttClientSettings settings) {
        var connectionOptions = new MqttConnectOptions();
        connectionOptions.setServerURIs(new String[]{settings.url()});

        var clientFactory = new DefaultMqttPahoClientFactory();
        clientFactory.setConnectionOptions(connectionOptions);

        return clientFactory;
    }

    /**
     * Configure the {@link MqttPahoMessageDrivenChannelAdapter} to handle inbound MQTT messages
     * from the root topic and device subtopics. The client ID ends with a '-rx' suffix.
     * @param settings the MQTT client connection settings
     * @param clientFactory the client factory
     * @return the channel adapter for inbound messages
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttChannelAdapter(MqttClientSettings settings,
                                                                  MqttPahoClientFactory clientFactory) {
        String rootTopic = settings.topic();
        var deviceTopic = rootTopic + DEVICE_SUBTOPIC + "+";
        var clientId = settings.clientId() + "-rx";

        return new MqttPahoMessageDrivenChannelAdapter(clientId, clientFactory, rootTopic, deviceTopic);
    }

    /**
     * <p>Configure the {@link IntegrationFlow} for inbound MQTT messages.</p>
     * <p>
     *     The root topic messages are routed to the 'root' channel, while the device subtopic messages
     *     are routed to 'device-in'. The incoming messages are assumed to have valid JSON payloads.
     * </p>
     * @param adapter the channel adapter for inbound messages
     * @return the integration flow for inbound messages
     */
    @Bean
    public IntegrationFlow mqttInbound(MqttPahoMessageDrivenChannelAdapter adapter) {
        return IntegrationFlows.from(adapter)
                .enrichHeaders(h -> h.header(MessageHeaders.CONTENT_TYPE, "application/json"))
                .route("headers['" + MqttHeaders.RECEIVED_TOPIC + "'].contains('device') ? 'device-in' : 'root'")
                .get();
    }

    /**
     * Configure the {@link MqttPahoMessageHandler} for sending MQTT messages. The client ID ends with a '-tx' suffix.
     * @param settings the MQTT client connection settings
     * @param mqttPahoClientFactory the client factory
     * @return the outbound message handler
     */
    @Bean
    public MessageHandler mqttOutbound(MqttClientSettings settings, MqttPahoClientFactory mqttPahoClientFactory) {
        String clientId = settings.clientId() + "-tx";
        return new MqttPahoMessageHandler(clientId, mqttPahoClientFactory);
    }

    /**
     * Configure the integration flow for incoming root topic messages. Outbound messages are converted to JSON.
     * @param rootTopicHandler the root topic message handler
     * @param mqttOutbound the outbound message handler
     * @return the root topic integration flow
     */
    @Bean
    public IntegrationFlow mqttRootTopicFlow(RootTopicHandler rootTopicHandler, MessageHandler mqttOutbound) {
        return IntegrationFlows.from("root")
                .handle(ConnectedMessage.class, rootTopicHandler)
                .transform(Transformers.toJson())
                .handle(mqttOutbound)
                .get();
    }

    /**
     * Configure the integration flow for incoming device subtopic messages.
     * @param deviceTopicHandler the device topic message handler
     * @return the device topic integration flow
     */
    @Bean
    public IntegrationFlow mqttDeviceTopicFlow(DeviceTopicHandler deviceTopicHandler) {
        return IntegrationFlows.from("device-in")
                .filter(CoasterMessage.class, msg -> msg instanceof IncomingDeviceTopicMessage)
                .handle(IncomingDeviceTopicMessage.class, deviceTopicHandler)
                .get();
    }
}
