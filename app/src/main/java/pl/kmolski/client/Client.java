package pl.kmolski.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class Client {
    public static void main(String[] args) throws Exception {
        var client = new MqttClient(Secrets.MQTT_SERVER_URL, "client");
        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(5);
        client.connect(options);

        System.out.println("Connected to host " + client.getServerURI() +"!");

        client.subscribe(Secrets.MQTT_TOPIC, (topic, msg) -> {
            System.out.println("New message on topic `" + topic + "`: " + msg);
        });
    }
}
