# hydrohomie

The smart drink coaster that lets you gather, view and analyze data on your water drinking habits.

The coaster device, based on the ESP32 MCU, uses a load sensor to measure the volume of water consumed,
which is then (optionally) sent over Wi-Fi & MQTT to the web application for storage and presentation purposes.

This started out as my 5th semester project for the Microprocessor and Embedded Systems course, for which I
prepared the hardware and firmware components. I the web application was created later, primarily as a means to
get some practice with new Java features and popular libraries, like Spring Integration/Security and Mockito.

Project components
------------------

- [Hardware](./hardware) - board & logic schematics in PDF & Autodesk EAGLE files
- [Firmware](./firmware) - firmware for the ESP32 chip, based on the Arduino platform
- [Web application](./web-app) - (optional) Spring Boot web app, provides coaster management & data visualization functionality
