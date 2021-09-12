package pl.kmolski.hydrohomie.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;
import pl.kmolski.hydrohomie.model.CoasterMessage.ConnectedMessage;
import pl.kmolski.hydrohomie.model.CoasterMessage.DeviceTopicMessage;
import pl.kmolski.hydrohomie.service.MqttDeviceTopicHandler;
import pl.kmolski.hydrohomie.service.MqttRootTopicHandler;

@Configuration
public class MqttConfiguration {

    private static final String DEVICE_TOPIC_SUFFIX = "/device/";

    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory(MqttClientSettings settings) {
        var connectionOptions = new MqttConnectOptions();
        connectionOptions.setServerURIs(new String[]{settings.getUrl()});

        var factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(connectionOptions);

        return factory;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttChannelAdapter(MqttClientSettings settings,
                                                                  MqttPahoClientFactory clientFactory) {
        var rootTopic = settings.getTopic();
        var deviceTopic = rootTopic + DEVICE_TOPIC_SUFFIX + "+";

        return new MqttPahoMessageDrivenChannelAdapter("web-app", clientFactory, rootTopic, deviceTopic);
    }

    @Bean
    public IntegrationFlow mqttInbound(MqttPahoMessageDrivenChannelAdapter adapter) {
        return IntegrationFlows.from(adapter)
                .enrichHeaders(h -> h.header(MessageHeaders.CONTENT_TYPE, "application/json"))
                .route("headers['" + MqttHeaders.RECEIVED_TOPIC + "'].contains('device') ? 'device' : 'root'")
                .get();
    }

    @Bean
    public IntegrationFlow mqttRootTopicFlow(MqttRootTopicHandler rootTopicHandler) {
        return IntegrationFlows.from("root").handle(ConnectedMessage.class, rootTopicHandler).get();
    }

    @Bean
    public IntegrationFlow mqttDeviceTopicFlow(MqttDeviceTopicHandler deviceTopicHandler) {
        return IntegrationFlows.from("device").handle(DeviceTopicMessage.class, deviceTopicHandler).get();
    }
}
