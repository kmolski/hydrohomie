#include <LiquidCrystal.h>
#include <HX711.h>

#include <WiFi.h>
#include <PubSubClient.h>

#include "secrets.h"

#include <atomic>
#include <chrono>
#include <mutex>

using namespace std::chrono;

LiquidCrystal lcd(18, 5, 17, 16, 4, 2);
HX711 scale;

WiFiClient wifi_client;
PubSubClient mqtt_client { wifi_client };

constexpr size_t DEVICE_NAME_SIZE = 64;
char device_name[DEVICE_NAME_SIZE] = { 0 };

TaskHandle_t buzzer_task;
steady_clock st_clock;
time_point<steady_clock> last_activity;
std::mutex time_mutex;

constexpr hours MAX_INACTIVE_TIME { 1 };

void buzzerTask(void*) {
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
std::atomic<float> last_load, load;
std::atomic<bool> request_tare { false };

void loadCellTask(void*) {
  while (true) {
    bool tare_requested = true;
    request_tare.compare_exchange_strong(tare_requested, false);
    if (tare_requested) { scale.tare(); }

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

enum class CoasterState {
  IDLE,
  MEASURE_FIRST,
  FIRST_COMPLETE,
  CANCEL,
  MEASURE_SECOND,
  SECOND_COMPLETE,
  STORE_RESULT
};

CoasterState current_state { CoasterState::IDLE };
std::atomic<bool> button1 { false }, button2 { false }, button3 { false };
double first_weight = 0.0, second_weight = 0.0, total = 0.0;

constexpr double WATER_DENSITY = 0.9975415;

void IRAM_ATTR button1ISR() { button1 = (digitalRead(27) == LOW); }
void IRAM_ATTR button2ISR() { button2 = (digitalRead(26) == LOW); }
void IRAM_ATTR button3ISR() { button3 = (digitalRead(25) == LOW); }

void setup() {
  Serial.begin(9600);

  lcd.begin(16, 2);
  scale.begin(32, 33);

  pinMode(19, OUTPUT);
  digitalWrite(19, LOW);
  last_activity = st_clock.now();
  xTaskCreatePinnedToCore(buzzerTask, "buzzer task", 1000, NULL, 1, &buzzer_task, 0);

  pinMode(25, INPUT);
  attachInterrupt(digitalPinToInterrupt(25), button3ISR, CHANGE);
  pinMode(26, INPUT);
  attachInterrupt(digitalPinToInterrupt(26), button2ISR, CHANGE);
  pinMode(27, INPUT);
  attachInterrupt(digitalPinToInterrupt(27), button1ISR, CHANGE);

// Load cell calibration code
//  scale.set_scale();
//  scale.tare();
//  lcd.setCursor(0, 0);
//  lcd.print("Place a weight and press S1.");
//  while (digitalRead(27) == HIGH);
//
//  lcd.setCursor(0, 0);
//  lcd.print(scale.get_units(10));
//  while (digitalRead(27) == HIGH);

  scale.set_scale(-867.8136055);
  scale.tare();

  xTaskCreatePinnedToCore(loadCellTask, "load cell task", 1000, NULL, 1, &load_cell_task, 0);

  byte mac[6] = { 0 };
  WiFi.macAddress(mac);
  snprintf(device_name, DEVICE_NAME_SIZE, "esp32-%x%x%x%x%x%x",
           mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);

  Serial.println(device_name);

  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
  }

  mqtt_client.setServer(MQTT_SERVER_IP, 1883);
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
  while (!mqtt_client.connected()) {
    Serial.println("trying to reconnect");
    if (mqtt_client.connect(device_name)) {
      Serial.println("connected to MQTT broker");
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
    snprintf(line_buf, 17, "T:%6.1lfml %c:%+3d",
             total, statusToChar(mqtt_client.state()), WiFi.RSSI());
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
        first_weight = load;
        lcd.setCursor(0, 0);
        lcd.print("S:");
        mqtt_client.publish(MQTT_TOPIC, "Start");
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
      double volume = (first_weight - second_weight) / WATER_DENSITY;

      snprintf(line_buf, 17, "D:%6.1lfml %02d:%02d", volume, hours, minutes);
      lcd.setCursor(0, 0);
      lcd.print(line_buf);

      if (button1) {
        current_state = CoasterState::MEASURE_SECOND;
        total += volume;
        lcd.setCursor(0, 0);
        lcd.print("V:");
        mqtt_client.publish(MQTT_TOPIC, line_buf);
        Serial.println(mqtt_client.state());
        delay(2000);
        current_state = CoasterState::IDLE;
      }

      if (button2) {
        current_state = CoasterState::CANCEL;
        lcd.setCursor(0, 0);
        lcd.print("Drink cancelled.");
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
