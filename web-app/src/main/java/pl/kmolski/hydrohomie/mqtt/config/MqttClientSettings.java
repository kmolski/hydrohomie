package pl.kmolski.hydrohomie.mqtt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>Configuration properties for the MQTT client.</p>
 * <p>
 *     This class defines the following properties:
 *     <ul>
 *         <li>mqtt.url - MQTT broker URL</li>
 *         <li>mqtt.topic - MQTT topic for the application</li>
 *         <li>mqtt.client-id - MQTT client ID</li>
 *     </ul>
 * </p>
 */
@ConfigurationProperties("mqtt")
public record MqttClientSettings(String url, String topic, String clientId) {}
