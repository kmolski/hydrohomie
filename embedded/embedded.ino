#include <LiquidCrystal.h>
#include <HX711.h>

#include <WiFi.h>

#include "secrets.h"

LiquidCrystal lcd(18, 5, 17, 16, 4, 2);
HX711 scale;

WiFiClient wifi;

void setup() {
  Serial.begin(9600);
  lcd.begin(16, 2);
  scale.begin(32, 33);
  pinMode(19, OUTPUT);
  pinMode(25, INPUT);
  pinMode(26, INPUT);
  pinMode(27, INPUT);
  digitalWrite(19, LOW);

  scale.set_scale(-7027.00 / 11.0);
  scale.set_offset(-283368);
  
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
  }
  char napis[16] = {0};
  IPAddress ip_addr = WiFi.localIP();
  snprintf(napis, 16, "%d.%d.%d.%d", ip_addr[0], ip_addr[1], ip_addr[2], ip_addr[3]);
  lcd.setCursor(0, 1);
  lcd.print(napis);
}

void loop() {
  char napis[16] = {0};
  snprintf(napis, 16, "Load: %lf", scale.get_units(10));
  lcd.setCursor(0, 0);
  lcd.print(napis);
  Serial.println(napis);
  int button_1 = digitalRead(25) == LOW ? 1 : 0;
  int button_2 = digitalRead(26) == LOW ? 1 : 0;
  int button_3 = digitalRead(27) == LOW ? 1 : 0;
  if (button_1 | button_2 | button_3) {
    digitalWrite(19, HIGH);
  } else {
    digitalWrite(19, LOW);
  }
  delay(100);
}
