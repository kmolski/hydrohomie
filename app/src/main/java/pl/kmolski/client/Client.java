package pl.kmolski.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.Date;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("Connection string and MQTT topic required!");
        }

        var client = new MqttClient(args[0], "client");
        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(5);
        client.connect(options);

        System.out.println("Connected to host " + client.getServerURI() +"!");

        client.subscribe(args[1], (topic, msg) -> {
            var date = new Date();
            System.out.println("[" + date.toString() + "] New message on topic `" + topic + "`: " + msg);
        });
    }
}
