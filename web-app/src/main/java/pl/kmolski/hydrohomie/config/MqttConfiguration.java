package pl.kmolski.hydrohomie.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import pl.kmolski.hydrohomie.service.MqttRootTopicHandler;

@Configuration
public class MqttConfiguration {

    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory(MqttClientSettings settings) {
        var connectionOptions = new MqttConnectOptions();
        connectionOptions.setServerURIs(new String[]{settings.getUrl()});

        var factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(connectionOptions);

        return factory;
    }

    @Bean
    public IntegrationFlow mqttInbound(MqttClientSettings settings, MqttPahoClientFactory clientFactory,
                                       MqttRootTopicHandler handler) {

        var channelAdapter = new MqttPahoMessageDrivenChannelAdapter("web-app", clientFactory, settings.getTopic());
        return IntegrationFlows.from(channelAdapter).handle(handler).get();
    }
}
