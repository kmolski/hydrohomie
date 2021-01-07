#include <LiquidCrystal.h>
#include <HX711.h>

LiquidCrystal lcd(18, 5, 17, 16, 4, 2);
HX711 scale;

void setup() {
  Serial.begin(9600);
  lcd.begin(16, 2);
  lcd.setCursor(0, 1);
  lcd.print("2020-12-13 13:33");
  scale.begin(32, 33);
  pinMode(19, OUTPUT);
  pinMode(25, INPUT);
  pinMode(26, INPUT);
  pinMode(27, INPUT);
  digitalWrite(19, LOW);
}

void loop() {
  char napis[16] = {0};
  snprintf(napis, 16, "Load: %ld", scale.read());
  lcd.setCursor(0, 0);
  lcd.print(napis);
  Serial.println(napis);
  int button_1 = digitalRead(25) == LOW ? 1 : 0;
  int button_2 = digitalRead(26) == LOW ? 1 : 0;
  int button_3 = digitalRead(27) == LOW ? 1 : 0;
  snprintf(napis, 16, "1: %d 2: %d 3: %d", button_1, button_2, button_3);
  lcd.setCursor(0, 1);
  lcd.print(napis);
  Serial.println(napis);
  if (button_1 | button_2 | button_3) {
    digitalWrite(19, HIGH);
  } else {
    digitalWrite(19, LOW);
  }
  delay(100);
}
