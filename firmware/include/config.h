#ifndef CONFIG
#define CONFIG

#include <chrono>

#include "secrets.h"

constexpr std::chrono::hours MAX_INACTIVE_TIME{1};

constexpr double LOAD_CELL_SCALE = -867.8136055;

#endif // CONFIG
