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
@Component
@ConfigurationProperties("mqtt")
public class MqttClientSettings {

    private String url;
    private String topic;
    private String clientId;

    /**
     * Return the MQTT broker URL
     * @return the broker URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the MQTT broker URL
     * @param url the broker URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Return the MQTT topic
     * @return the MQTT topic for the application
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Set the MQTT topic
     * @param topic the MQTT topic for the application
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Return the MQTT client ID
     * @return the MQTT client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Set the MQTT client ID
     * @param clientId the MQTT client ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
