# hydrohomie - firmware

Firmware for the smart coaster device, based on the Arduino ESP32 platform and open-source libraries.

Built with:
-----------

- PlatformIO toolset
- Arduino ESP32 platform
- [Arduino LiquidCrystal library](https://www.arduino.cc/en/Reference/LiquidCrystal)
- [Arduino WiFi library](https://www.arduino.cc/en/Reference/WiFi)
- [knolleary/PubSubClient](https://github.com/knolleary/pubsubclient)
- [bblanchon/ArduinoJson](https://github.com/bblanchon/ArduinoJson)
- [bogde/HX711](https://github.com/bogde/HX711)

Setup instructions:
-----------

### Load cell calibration
1. Configure the target device in `platformio.ini`.
2. Uncomment the load cell calibration code at `src/embedded.cpp:163-171`.
3. Build & flash the device with `pio run -t upload`.
4. Wait for the device to start and press S1 with a known weight on the coaster.
5. Write down the value shown on the LCD screen as "x".
6. Calculate the load cell scale as `x / m`, where "m" is the known weight in grams.
7. Set the load cell scale in `include/config.h`.
8. Comment/remove the load cell calibration code.

### Wi-Fi and MQTT configuration
9. Set the Wi-Fi SSID, Wi-Fi password, MQTT broker IP and MQTT topic in `include/secrets.h`.
10. Adjust the maximum inactivity time in `include/config.h`.

### Final setup
11. Build & flash the device with `pio run -t upload`.

License:
--------

[MIT License](https://opensource.org/licenses/MIT)
