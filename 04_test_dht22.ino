/**
 * TEST 04: DHT22 DIGITAL TEMPERATURE & HUMIDITY SENSOR (ESP32)
 * Hardware Pin: GPIO 15
 */

#include <Arduino.h>
#include <DHT.h>

#define DHT_PIN     15
#define DHT_TYPE    DHT22   // AM2302 / DHT22

DHT dht(DHT_PIN, DHT_TYPE);

unsigned long lastReadTime = 0;
int readCount = 0;

void setup() {
    Serial.begin(115200);
    delay(1000);

    // Internal pull-up helps maintain strong signal
    pinMode(DHT_PIN, INPUT_PULLUP);

    Serial.println("\n==================================================");
    Serial.println("     ESP32 STANDALONE DHT22 SENSOR TEST");
    Serial.println("==================================================");
    Serial.println("Pin Config: DATA -> GPIO 15 | VCC -> 3V3 | GND -> GND");
    Serial.println("Warming up DHT22 sensor (requires ~2 seconds)...");

    dht.begin();
    delay(2000);
    Serial.println("Sensor Ready! Starting continuous readings...\n");
}

void loop() {
    // DHT22 samples once every 2.5 seconds
    if (millis() - lastReadTime >= 2500) {
        lastReadTime = millis();
        readCount++;

        float humidity = dht.readHumidity();
        float tempC = dht.readTemperature();       // Celsius
        float tempF = dht.readTemperature(true);   // Fahrenheit

        // Check if any reads failed
        if (isnan(humidity) || isnan(tempC)) {
            Serial.print("[#"); Serial.print(readCount);
            Serial.println("] [ERROR] Failed to read from DHT22 sensor!");
            Serial.println("    -> Check 1: Is DATA plugged firmly into GPIO 15?");
            Serial.println("    -> Check 2: Try plugging VCC into VIN (5V) instead of 3V3.");
            Serial.println("    -> Check 3: Ensure GND is connected to ESP32 GND.");
            Serial.println("--------------------------------------------------");
            return;
        }

        // Compute heat index in Celsius
        float heatIndexC = dht.computeHeatIndex(tempC, humidity, false);

        // Display results
        Serial.print("[#"); Serial.print(readCount); Serial.print("] ");
        Serial.print("Temp: ");
        Serial.print(tempC, 1);
        Serial.print(" °C (");
        Serial.print(tempF, 1);
        Serial.print(" °F) | Humidity: ");
        Serial.print(humidity, 1);
        Serial.print(" % | Heat Index: ");
        Serial.print(heatIndexC, 1);
        Serial.print(" °C");

        // Safe medicine storage alerts
        if (tempC > 30.0f) {
            Serial.print("  [!] HOT (>30°C)");
        }
        if (humidity > 70.0f) {
            Serial.print("  [!] HIGH HUMIDITY (>70%)");
        }
        Serial.println();
    }
}
