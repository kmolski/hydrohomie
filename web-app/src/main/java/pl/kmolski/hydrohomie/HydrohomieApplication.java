package pl.kmolski.hydrohomie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import pl.kmolski.hydrohomie.account.config.AdminAccountSettings;
import pl.kmolski.hydrohomie.mqtt.config.MqttClientSettings;

import java.time.Clock;

@SpringBootApplication
@EnableConfigurationProperties({AdminAccountSettings.class, MqttClientSettings.class})
public class HydrohomieApplication {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    public static void main(String[] args) {
        SpringApplication.run(HydrohomieApplication.class, args);
    }
}
