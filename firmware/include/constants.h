#ifndef CONSTANTS
#define CONSTANTS

#include <ArduinoJson.h>

#include "config.h"

// MQTT constants
constexpr size_t DEVICE_NAME_LEN = 128;

constexpr char MQTT_DEVICE_TOPIC_SUFFIX[] = "/device/";
constexpr size_t DEVICE_TOPIC_LEN = strlen(MQTT_TOPIC) + strlen(MQTT_DEVICE_TOPIC_SUFFIX) + DEVICE_NAME_LEN;

// JSON constants
constexpr size_t JSON_DOC_LEN = 256;
using JsonDoc = StaticJsonDocument<JSON_DOC_LEN>;

// Other constants
constexpr float WATER_DENSITY = 0.9975415;

#endif // CONSTANTS
