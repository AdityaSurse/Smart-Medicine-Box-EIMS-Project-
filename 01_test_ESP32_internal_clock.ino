#include <WiFi.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <time.h>

// =============================================================================
// 1. ENTER YOUR WI-FI CREDENTIALS HERE
// =============================================================================
const char* WIFI_SSID     = "WIFI NAME";      // <-- Put your Wi-Fi or Mobile Hotspot name here
const char* WIFI_PASSWORD = "WIFI PASSWORD";  // <-- Put your Wi-Fi password here

// =============================================================================
// 2. TIMEZONE CONFIGURATION (IST: UTC + 5:30)
// =============================================================================
// 5 hours * 3600 + 30 mins * 60 = 19800 seconds
const long  GMT_OFFSET_SEC      = 19800; 
const int   DAYLIGHT_OFFSET_SEC = 0;
const char* NTP_SERVER1         = "pool.ntp.org";
const char* NTP_SERVER2         = "time.google.com";

#define I2C_SDA_PIN   21
#define I2C_SCL_PIN   22
#define OLED_ADDR     0x3C

Adafruit_SSD1306 display(128, 64, &Wire, -1);
bool blinkTick = false;

// Universal wakeup for clone OLED screens
void wakeOLED() {
    Wire.beginTransmission(OLED_ADDR);
    Wire.write(0x00);
    Wire.write(0x8D); Wire.write(0x14); // SSD1306 Charge Pump ON
    Wire.write(0xAD); Wire.write(0x8B); // SH1106 DC-DC Pump ON
    Wire.write(0x81); Wire.write(0xFF); // Max Brightness
    Wire.write(0xA4); Wire.write(0xAF); // Display ON
    Wire.endTransmission();
}

void setup() {
    Serial.begin(115200);
    delay(500);

    Serial.println("\n=============================================");
    Serial.println("  ESP32 LIVE ATOMIC CLOCK (NTP + OLED)");
    Serial.println("=============================================");

    // 1. Initialize OLED Screen
    Wire.begin(I2C_SDA_PIN, I2C_SCL_PIN);
    if (!display.begin(SSD1306_SWITCHCAPVCC, OLED_ADDR)) {
        display.begin(SSD1306_SWITCHCAPVCC, 0x3D);
    }
    wakeOLED();

    // Show "Connecting" on OLED
    display.clearDisplay();
    display.setTextColor(SSD1306_WHITE);
    display.drawRect(0, 0, 128, 64, SSD1306_WHITE);
    display.setTextSize(1);
    display.setCursor(14, 15);
    display.println("CONNECTING TO WIFI");
    display.setCursor(10, 35);
    display.println("Fetching Live Time...");
    display.display();

    // 2. Connect to Wi-Fi
    Serial.printf("Connecting to Wi-Fi: %s ", WIFI_SSID);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 25) {
        delay(500);
        Serial.print(".");
        attempts++;
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n[OK] Wi-Fi Connected!");
        Serial.printf("IP Address: %s\n", WiFi.localIP().toString().c_str());

        // 3. Sync with Internet Atomic Clock (NTP)
        Serial.println("Syncing time with Internet Atomic Clock...");
        configTime(GMT_OFFSET_SEC, DAYLIGHT_OFFSET_SEC, NTP_SERVER1, NTP_SERVER2);

        struct tm timeinfo;
        if (getLocalTime(&timeinfo, 10000)) { // Wait up to 10s for sync
            Serial.println("[SUCCESS] Live Atomic Time Synced!");
        } else {
            Serial.println("[!] NTP sync timed out. Using default fallback.");
        }
    } else {
        Serial.println("\n[!] Wi-Fi connection failed. Running on fallback clock (8:08 PM).");
        // Fallback: set to 20:08:00 if Wi-Fi password was wrong
        struct tm tm;
        tm.tm_year = 2026 - 1900;
        tm.tm_mon  = 10 - 1;
        tm.tm_mday = 30;
        tm.tm_hour = 20;
        tm.tm_min  = 8;
        tm.tm_sec  = 0;
        time_t t = mktime(&tm);
        struct timeval now = { .tv_sec = t };
        settimeofday(&now, NULL);
    }

    delay(1000);
}

void loop() {
    // 1. Read Current Live Time
    time_t nowTime;
    struct tm *t;
    time(&nowTime);
    t = localtime(&nowTime);

    // 2. 12-Hour Conversion (AM / PM)
    uint8_t hour12 = t->tm_hour % 12;
    if (hour12 == 0) hour12 = 12;
    bool isPM = (t->tm_hour >= 12);

    char timeStr[16];
    char dateStr[20];
    blinkTick = !blinkTick;

    snprintf(timeStr, sizeof(timeStr), "%02d:%02d:%02d", hour12, t->tm_min, t->tm_sec);
    snprintf(dateStr, sizeof(dateStr), "%04d-%02d-%02d", t->tm_year + 1900, t->tm_mon + 1, t->tm_mday);

    // 3. Print to Serial Monitor
    Serial.printf("Live Time: %s  %s %s  %s\n",
                  dateStr, timeStr, isPM ? "PM" : "AM", blinkTick ? "[*]" : "[ ]");

    // 4. Render on 0.96" OLED
    display.clearDisplay();

    // Decorative Outer Border
    display.drawRect(0, 0, 128, 64, SSD1306_WHITE);

    // Header Title
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(18, 5);
    display.println("SMART MED BOX");
    display.drawLine(6, 16, 121, 16, SSD1306_WHITE);

    // Big Live Clock (Size 2 Font)
    display.setTextSize(2);
    display.setCursor(8, 22);
    char largeClock[12];
    snprintf(largeClock, sizeof(largeClock), "%02d:%02d", hour12, t->tm_min);
    display.print(largeClock);

    // Smaller Seconds & AM/PM
    display.setTextSize(1);
    display.setCursor(76, 22);
    display.printf(":%02d", t->tm_sec);
    display.setCursor(76, 31);
    display.print(isPM ? "PM" : "AM");

    // Heartbeat Blinking Dot
    display.setCursor(102, 26);
    display.print(blinkTick ? "*" : " ");

    // Bottom Divider
    display.drawLine(6, 44, 121, 44, SSD1306_WHITE);

    // Date & Live Status
    display.setTextSize(1);
    display.setCursor(10, 50);
    display.print(dateStr);

    display.setCursor(80, 50);
    if (WiFi.status() == WL_CONNECTED) {
        display.print("LIVE-NTP");
    } else {
        display.print("OFFLINE");
    }

    // Refresh Display
    display.display();

    delay(1000); // 1-second live tick
}
