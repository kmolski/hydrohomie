package pl.kmolski.hydrohomie.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class MqttHandlerIT extends WebFluxControllerIT {

    private static final MountableFile MOSQUITTO_CONFIG_FILE = MountableFile.forClasspathResource("config/mosquitto.conf");

    private static final GenericContainer<?> MOSQUITTO = new GenericContainer<>("eclipse-mosquitto:2.0.14")
            .withCopyFileToContainer(MOSQUITTO_CONFIG_FILE, "/mosquitto/config/mosquitto.conf")
            .withExposedPorts(1883);

    @Autowired
    // TODO: drop visibility
    protected MqttPahoClientFactory mqttPahoClientFactory;

    protected IMqttClient mqttTestClient;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    static String getMosquittoUrl() {
        return String.format("tcp://%s:%s", MOSQUITTO.getHost(), MOSQUITTO.getMappedPort(1883));
    }

    @DynamicPropertySource
    static void setupMosquittoContainer(DynamicPropertyRegistry propertyRegistry) {
        MOSQUITTO.start();

        propertyRegistry.add("mqtt.url", MqttHandlerIT::getMosquittoUrl);
    }

    @BeforeEach
    void setupMqttTestClient() throws MqttException {
        mqttTestClient = mqttPahoClientFactory.getClientInstance(getMosquittoUrl(), "mock-device");
        mqttTestClient.connect();
    }

    @AfterEach
    void closeMqttTestClient() {
        try {
            mqttTestClient.close();
        } catch (MqttException ignored) {}
    }

    protected void sendMqttMessage(String topic, Object message) throws JsonProcessingException, MqttException {
        var messageBytes = objectMapper.writeValueAsBytes(message);
        mqttTestClient.publish(topic, messageBytes, 0, false);
    }

    protected void assertMqttMessagesInOrder(String topic, IMqttMessageListener... assertions) throws InterruptedException, MqttException {
        var exceptions = new ArrayList<>(assertions.length);
        var latch = new CountDownLatch(assertions.length);
        mqttTestClient.subscribe(topic, (t, msg) -> {
            try {
                assertions[(int) (assertions.length - latch.getCount())].messageArrived(t, msg);
                latch.countDown();
            } catch (Exception e) {
                exceptions.add(e);
            }
        });
        assertTrue(exceptions.isEmpty(), "Assertions threw exceptions: " + exceptions);
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out");
    }
}
