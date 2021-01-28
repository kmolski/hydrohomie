#include <LiquidCrystal.h>
#include <HX711.h>

#include <WiFi.h>
#include <PubSubClient.h>

#include "secrets.h"

#include <atomic>

LiquidCrystal lcd(18, 5, 17, 16, 4, 2);
HX711 scale;

WiFiClient wifi_client;
PubSubClient mqtt_client { wifi_client };

TaskHandle_t load_cell_task;
std::atomic<float> load;

void loadCellTask(void*) {
  while (true) {
    load = scale.get_units(10);
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

std::atomic<CoasterState> current_state { CoasterState::IDLE };
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

  pinMode(25, INPUT);
  attachInterrupt(digitalPinToInterrupt(25), button3ISR, CHANGE);
  pinMode(26, INPUT);
  attachInterrupt(digitalPinToInterrupt(26), button2ISR, CHANGE);
  pinMode(27, INPUT);
  attachInterrupt(digitalPinToInterrupt(27), button1ISR, CHANGE);

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
  //scale.set_offset(-283368);

  xTaskCreatePinnedToCore(loadCellTask, "load cell task", 1000, NULL, 1, &load_cell_task, 0);

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
    if (mqtt_client.connect("esp32")) {
      Serial.println("connected to MQTT broker");
    } else {
      Serial.println("trying again");
      delay(5000);
    }    
  }
  mqtt_client.loop();

  char line_buf[17] = {0};
  snprintf(line_buf, 17, "Load: %*.2lfg", 9, double(load));
  lcd.setCursor(0, 0);
  lcd.print(line_buf);

  snprintf(line_buf, 17, "%c:%+3d T:%6.1lfml",
           statusToChar(mqtt_client.state()), WiFi.RSSI(), total);
  lcd.setCursor(0, 1);
  lcd.print(line_buf);

  switch (current_state) {
    case CoasterState::IDLE: {
      if (button1) {
        current_state = CoasterState::MEASURE_FIRST;
        first_weight = load;
        lcd.setCursor(0, 0);
        lcd.print("Start");
        mqtt_client.publish(MQTT_TOPIC, "Start");
        Serial.print(mqtt_client.state());
        delay(1000);
        current_state = CoasterState::FIRST_COMPLETE;
      }
      break;
    }

    case CoasterState::FIRST_COMPLETE: {
      if (button1) {
        current_state = CoasterState::MEASURE_SECOND;
        second_weight = load;
        lcd.setCursor(0, 0);
        double volume = (first_weight - second_weight) / WATER_DENSITY;
        total += volume;
        snprintf(line_buf, 17, "Volume: %*.2lfml", 6, volume);
        lcd.print(line_buf);
        mqtt_client.publish(MQTT_TOPIC, line_buf);
        Serial.print(mqtt_client.state());
        delay(1000);
        current_state = CoasterState::IDLE;
      }
      break;
    }

    default:
      break;
  }

  if (button2) {
    total = 0.0;
  }

  if (button3) {
    scale.tare();
  }

  if (button1 | button2 | button3) {
    digitalWrite(19, HIGH);
  } else {
    digitalWrite(19, LOW);
  }

  delay(100);
}
