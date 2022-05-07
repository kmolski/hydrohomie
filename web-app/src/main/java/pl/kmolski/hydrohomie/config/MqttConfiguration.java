package pl.kmolski.hydrohomie.config;

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
import pl.kmolski.hydrohomie.handler.MqttDeviceTopicHandler;
import pl.kmolski.hydrohomie.handler.MqttRootTopicHandler;
import pl.kmolski.hydrohomie.model.CoasterMessage;
import pl.kmolski.hydrohomie.model.CoasterMessage.ConnectedMessage;
import pl.kmolski.hydrohomie.model.CoasterMessage.IncomingDeviceTopicMessage;


@Configuration
public class MqttConfiguration {

    public static final String DEVICE_TOPIC_SUFFIX = "/device/";

    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory(MqttClientSettings settings) {
        var connectionOptions = new MqttConnectOptions();
        connectionOptions.setServerURIs(new String[]{settings.getUrl()});

        var clientFactory = new DefaultMqttPahoClientFactory();
        clientFactory.setConnectionOptions(connectionOptions);

        return clientFactory;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttChannelAdapter(MqttClientSettings settings,
                                                                  MqttPahoClientFactory clientFactory) {
        String rootTopic = settings.getTopic();
        var deviceTopic = rootTopic + DEVICE_TOPIC_SUFFIX + "+";
        var clientId = settings.getClientId() + "-rx";

        return new MqttPahoMessageDrivenChannelAdapter(clientId, clientFactory, rootTopic, deviceTopic);
    }

    @Bean
    public IntegrationFlow mqttInbound(MqttPahoMessageDrivenChannelAdapter adapter) {
        return IntegrationFlows.from(adapter)
                .enrichHeaders(h -> h.header(MessageHeaders.CONTENT_TYPE, "application/json"))
                .route("headers['" + MqttHeaders.RECEIVED_TOPIC + "'].contains('device') ? 'device-in' : 'root'")
                .get();
    }

    @Bean
    public MessageHandler mqttOutbound(MqttClientSettings settings, MqttPahoClientFactory mqttPahoClientFactory) {
        String clientId = settings.getClientId() + "-tx";
        return new MqttPahoMessageHandler(clientId, mqttPahoClientFactory);
    }

    @Bean
    public IntegrationFlow mqttRootTopicFlow(MqttRootTopicHandler rootTopicHandler, MessageHandler mqttOutbound) {
        return IntegrationFlows.from("root")
                .handle(ConnectedMessage.class, rootTopicHandler)
                .transform(Transformers.toJson())
                .handle(mqttOutbound)
                .get();
    }

    @Bean
    public IntegrationFlow mqttDeviceTopicFlow(MqttDeviceTopicHandler deviceTopicHandler) {
        return IntegrationFlows.from("device-in")
                .filter(CoasterMessage.class, msg -> msg instanceof IncomingDeviceTopicMessage)
                .handle(IncomingDeviceTopicMessage.class, deviceTopicHandler)
                .get();
    }
}
