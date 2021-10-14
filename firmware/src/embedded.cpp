#include <HX711.h>
#include <LiquidCrystal.h>

#include <ArduinoJson.h>
#include <PubSubClient.h>
#include <WiFi.h>

#include "secrets.h"

#include <atomic>
#include <chrono>
#include <mutex>

using namespace std::chrono;

LiquidCrystal lcd(18, 5, 17, 16, 4, 2);
HX711 scale;

WiFiClient wifi_client;
PubSubClient mqtt_client{wifi_client};

constexpr size_t DEVICE_NAME_LEN = 128;
char device_name[DEVICE_NAME_LEN] = {0};

constexpr char MQTT_DEVICE_TOPIC_SUFFIX[] = "/device/";
constexpr size_t DEVICE_TOPIC_LEN = strlen(MQTT_TOPIC) + strlen(MQTT_DEVICE_TOPIC_SUFFIX) + DEVICE_NAME_LEN;
char device_topic[DEVICE_TOPIC_LEN] = {0};

constexpr size_t JSON_DOC_LEN = 256;
using JsonDoc = StaticJsonDocument<JSON_DOC_LEN>;

TaskHandle_t buzzer_task;
steady_clock st_clock;
time_point<steady_clock> last_activity;
std::mutex time_mutex;

constexpr hours MAX_INACTIVE_TIME{1};

void buzzerTask(void *) {
    bool buzzer_state = LOW;
    while (true) {
        time_mutex.lock();
        if (st_clock.now() - last_activity > MAX_INACTIVE_TIME) {
            buzzer_state = !buzzer_state;
        } else {
            buzzer_state = LOW;
        }
        time_mutex.unlock();

        digitalWrite(19, buzzer_state);
        delay(1000);
    }
}

TaskHandle_t load_cell_task;
std::atomic<float> last_load, load, total{0.0}, first_weight{0.0};
std::atomic<bool> request_tare{false};

void loadCellTask(void *) {
    while (true) {
        bool tare_requested = true;
        request_tare.compare_exchange_strong(tare_requested, false);
        if (tare_requested) {
            scale.tare();
        }

        last_load = float(load);
        load = scale.get_units(10);

        if (abs(last_load - load) > 10.0) {
            time_mutex.lock();
            last_activity = st_clock.now();
            time_mutex.unlock();
        }

        delay(50);
    }
}

enum class CoasterState { IDLE, MEASURE_FIRST, FIRST_COMPLETE, DISCARD, MEASURE_SECOND, SECOND_COMPLETE, STORE_RESULT };

CoasterState current_state{CoasterState::IDLE};
std::atomic<bool> button1{false}, button2{false}, button3{false};
float second_weight = 0.0;

void subscriptionCallback(char topic[], byte *payload, unsigned int length) {
    JsonDoc doc;
    if (strcmp(topic, device_topic) == 0) {
        deserializeJson(doc, payload, length);

        const char *message_type = doc["type"].as<char *>();
        if (strcmp("listening", message_type) == 0) {
            total = doc["initTotal"].as<float>();

            JsonVariant init_load = doc["initLoad"].as<JsonVariant>();
            if (!init_load.isNull()) {
                current_state = CoasterState::FIRST_COMPLETE;
                first_weight = init_load.as<float>();
            }
        }
    }
}

constexpr float WATER_DENSITY = 0.9975415;

void IRAM_ATTR button1ISR() { button1 = (digitalRead(27) == LOW); }
void IRAM_ATTR button2ISR() { button2 = (digitalRead(26) == LOW); }
void IRAM_ATTR button3ISR() { button3 = (digitalRead(25) == LOW); }

void setup() {
    Serial.begin(115200);

    lcd.begin(16, 2);
    scale.begin(32, 33);

    pinMode(25, INPUT);
    attachInterrupt(digitalPinToInterrupt(25), button3ISR, CHANGE);
    pinMode(26, INPUT);
    attachInterrupt(digitalPinToInterrupt(26), button2ISR, CHANGE);
    pinMode(27, INPUT);
    attachInterrupt(digitalPinToInterrupt(27), button1ISR, CHANGE);

    pinMode(19, OUTPUT);
    digitalWrite(19, LOW);
    last_activity = st_clock.now();
    xTaskCreatePinnedToCore(buzzerTask, "buzzer task", 1000, NULL, 1, &buzzer_task, 0);

    // Load cell calibration code
    // scale.set_scale();
    // scale.tare();
    // lcd.setCursor(0, 0);
    // lcd.print("Place a weight and press S1.");
    // while (digitalRead(27) == HIGH);
    //
    // lcd.setCursor(0, 0);
    // lcd.print(scale.get_units(10));
    // while (digitalRead(27) == HIGH);

    scale.set_scale(-867.8136055);
    scale.tare();

    xTaskCreatePinnedToCore(loadCellTask, "load cell task", 1000, NULL, 1, &load_cell_task, 0);

    byte mac[6] = {0};
    WiFi.macAddress(mac);
    snprintf(device_name, DEVICE_NAME_LEN, "esp32-%x%x%x%x%x%x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
    Serial.println(device_name);

    snprintf(device_topic, DEVICE_TOPIC_LEN, "%s%s%s", MQTT_TOPIC, MQTT_DEVICE_TOPIC_SUFFIX, device_name);
    Serial.println(device_topic);

    WiFi.begin(WIFI_SSID, WIFI_PASS);
    while (WiFi.status() != WL_CONNECTED) {
        delay(100);
    }

    mqtt_client.setServer(MQTT_SERVER_IP, 1883);
    mqtt_client.setCallback(subscriptionCallback);
}

char statusToChar(int state) {
    switch (state) {
        case MQTT_CONNECTED:
            return 'C';
        case MQTT_DISCONNECTED:
            return 'D';
        default:
            return 'N';
    }
}

void loop() {
    byte json_bytes[1024] = {0};

    while (!mqtt_client.connected()) {
        Serial.println("trying to reconnect");
        if (mqtt_client.connect(device_name)) {
            Serial.println("connected to MQTT broker");
            mqtt_client.subscribe(device_topic);

            JsonDoc doc;
            doc["device"] = device_name;
            doc["type"] = "connected";

            size_t bytes = serializeJson(doc, json_bytes);
            mqtt_client.publish(MQTT_TOPIC, json_bytes, bytes);
        } else {
            Serial.println("trying again");
            delay(5000);
        }
    }
    mqtt_client.loop();

    char line_buf[17] = {0};
    lcd.setCursor(0, 1);

    time_mutex.lock();
    if (st_clock.now() - last_activity > MAX_INACTIVE_TIME) {
        lcd.print("Inactive for 1hr");
    } else {
        snprintf(line_buf, 17, "T:%6.1lfml %c:%+3d", float(total), statusToChar(mqtt_client.state()), WiFi.RSSI());
        lcd.print(line_buf);
    }

    int min_since_active = duration_cast<minutes>(st_clock.now() - last_activity).count();
    int hours = min_since_active / 60;
    int minutes = min_since_active % 60;
    time_mutex.unlock();

    switch (current_state) {
        case CoasterState::IDLE: {
            snprintf(line_buf, 17, "L:%7.2fg %02d:%02d", float(load), hours, minutes);
            lcd.setCursor(0, 0);
            lcd.print(line_buf);

            if (button1) {
                current_state = CoasterState::MEASURE_FIRST;
                first_weight = float(load);
                lcd.setCursor(0, 0);
                lcd.print("S:");

                JsonDoc doc;
                doc["device"] = device_name;
                doc["type"] = "begin";
                doc["load"] = float(first_weight);

                size_t bytes = serializeJson(doc, json_bytes);
                mqtt_client.publish(device_topic, json_bytes, bytes);

                Serial.println(mqtt_client.state());
                delay(2000);
                current_state = CoasterState::FIRST_COMPLETE;
            }

            if (button2) {
                total = 0.0;
                lcd.setCursor(0, 0);
                lcd.print("Total cleared.  ");
                delay(1000);
            }
            break;
        }

        case CoasterState::FIRST_COMPLETE: {
            second_weight = load;
            float volume = (first_weight - second_weight) / WATER_DENSITY;

            snprintf(line_buf, 17, "D:%6.1lfml %02d:%02d", volume, hours, minutes);
            lcd.setCursor(0, 0);
            lcd.print(line_buf);

            if (button1) {
                current_state = CoasterState::MEASURE_SECOND;
                lcd.setCursor(0, 0);
                lcd.print("V:");

                float old_total = float(total);
                float new_total = old_total + volume;
                while (!total.compare_exchange_strong(old_total, new_total)) {
                    new_total = old_total + volume;
                }

                JsonDoc doc;
                doc["device"] = device_name;
                doc["type"] = "end";
                doc["volume"] = volume;

                size_t bytes = serializeJson(doc, json_bytes);
                mqtt_client.publish(device_topic, json_bytes, bytes);

                Serial.println(mqtt_client.state());
                delay(2000);
                current_state = CoasterState::IDLE;
            }

            if (button2) {
                current_state = CoasterState::DISCARD;
                lcd.setCursor(0, 0);
                lcd.print("Drink discarded.");

                JsonDoc doc;
                doc["device"] = device_name;
                doc["type"] = "discard";

                size_t bytes = serializeJson(doc, json_bytes);
                mqtt_client.publish(device_topic, json_bytes, bytes);

                delay(1000);
                current_state = CoasterState::IDLE;
            }
            break;
        }

        default:
            break;
    }

    if (button3) {
        lcd.setCursor(0, 0);
        lcd.print("Calibrating...  ");
        request_tare = true;
        delay(1000);
    }

    delay(100);
}
