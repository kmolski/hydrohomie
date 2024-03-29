package pl.kmolski.hydrohomie.mqtt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>Configuration properties for the MQTT client.
 * <p>
 * This class defines the following properties:
 * <ul>
 *     <li>mqtt.url - MQTT broker URL</li>
 *     <li>mqtt.topic - MQTT topic for the application</li>
 *     <li>mqtt.client-id - MQTT client ID</li>
 * </ul>
 */
@ConfigurationProperties("mqtt")
public record MqttClientSettings(String url, String topic, String clientId) {}
