/**
 * TEST 02: 3KG LOAD CELL TEST (CALIBRATION FACTOR = 700.0)
 * Tailored for 3kg Load Cell + HX711 on ESP32 DevKit
 * DT -> GPIO 16 | SCK -> GPIO 4 | VCC -> VIN (5V) | GND -> GND
 */

#include <Arduino.h>
#include <HX711.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#define HX711_DT_PIN   16
#define HX711_SCK_PIN  4

#define I2C_SDA_PIN    21
#define I2C_SCL_PIN    22
#define OLED_ADDR      0x3C

HX711 scale;
Adafruit_SSD1306 display(128, 64, &Wire, -1);
bool oledFound = false;

// EXACT FIXED FACTOR FOR 3KG LOAD CELL
const float CAL_FACTOR = 700.0f;

// 10-sample moving average filter buffer
const int FILTER_SIZE = 10;
float weightBuffer[FILTER_SIZE];
int bufferIndex = 0;
bool bufferFilled = false;

float lastSmoothedWeight = 0.0f;
unsigned long lastPrintTime = 0;

void setup() {
    Serial.begin(115200);
    delay(1000);

    Serial.println("\n==================================================");
    Serial.println("     ESP32 3KG LOAD CELL SCALE (FACTOR: 700.0)");
    Serial.println("==================================================");
    Serial.println("Load Cell Capacity: 3 kg");
    Serial.println("Calibration Factor: 700.0 counts/gram");
    Serial.println("Pin Config: DT -> GPIO 16 | SCK -> GPIO 4");
    Serial.println("--------------------------------------------------");

    for (int i = 0; i < FILTER_SIZE; i++) weightBuffer[i] = 0.0f;

    // Optional OLED Check
    Wire.begin(I2C_SDA_PIN, I2C_SCL_PIN);
    oledFound = display.begin(SSD1306_SWITCHCAPVCC, OLED_ADDR);
    if (!oledFound) oledFound = display.begin(SSD1306_SWITCHCAPVCC, 0x3D);
    if (oledFound) {
        display.clearDisplay();
        display.setTextSize(1);
        display.setTextColor(SSD1306_WHITE);
        display.setCursor(15, 20);
        display.println("3KG SCALE READY");
        display.display();
    }

    // Initialize HX711
    scale.begin(HX711_DT_PIN, HX711_SCK_PIN);

    int timeout = 0;
    while (!scale.is_ready() && timeout < 25) {
        delay(100);
        timeout++;
    }

    if (scale.is_ready()) {
        scale.set_scale(CAL_FACTOR);
        scale.tare(30); // Auto-zero with scale empty
        Serial.println("[OK] Scale initialized and zeroed to 0.00 g!");
    } else {
        Serial.println("[ERROR] HX711 not responding! Check DT (GPIO 16) and SCK (GPIO 4).");
    }

    Serial.println("\nReady! Put your object on the scale.");
    Serial.println("Tip: Type 't' and press Enter anytime to zero the scale.\n");
}

void loop() {
    if (scale.is_ready()) {
        // Inverted (-) ensures pressing DOWN gives POSITIVE (+) weight
        float rawReading = -scale.get_units(3);

        // Circular filter buffer
        weightBuffer[bufferIndex] = rawReading;
        bufferIndex = (bufferIndex + 1) % FILTER_SIZE;
        if (bufferIndex == 0) bufferFilled = true;

        int count = bufferFilled ? FILTER_SIZE : bufferIndex;
        if (count == 0) count = 1;
        float sum = 0.0f;
        for (int i = 0; i < count; i++) sum += weightBuffer[i];
        float smoothedWeight = sum / count;

        // Clamp minor zero jitter (-0.15 to +0.10 -> 0.00)
        if (smoothedWeight > -0.15f && smoothedWeight < 0.10f) smoothedWeight = 0.0f;

        // Check stability
        bool isStable = abs(smoothedWeight - lastSmoothedWeight) < 0.25f;
        lastSmoothedWeight = smoothedWeight;

        // Print to Serial every 350ms
        if (millis() - lastPrintTime >= 350) {
            lastPrintTime = millis();

            Serial.printf("Weight: %6.1f g  |  Status: %s  |  Factor: %.1f\n",
                          smoothedWeight,
                          isStable ? "[ STABLE ] " : "[SETTLING]",
                          CAL_FACTOR);

            // Optional OLED Display
            if (oledFound) {
                display.clearDisplay();
                display.drawRoundRect(0, 0, 128, 64, 4, SSD1306_WHITE);
                display.setTextSize(1);
                display.setTextColor(SSD1306_WHITE);
                display.setCursor(20, 5);
                display.print("3KG SMART SCALE");
                display.drawLine(4, 15, 123, 15, SSD1306_WHITE);

                display.setTextSize(2);
                display.setCursor(22, 24);
                display.printf("%5.1f g", smoothedWeight);

                display.setTextSize(1);
                display.setCursor(14, 48);
                display.print(isStable ? "Status: STABLE" : "Status: MOTION");
                display.display();
            }
        }
    }

    // Handle 't' command to Tare
    if (Serial.available() > 0) {
        String cmd = Serial.readStringUntil('\n');
        cmd.trim();

        if (cmd.equalsIgnoreCase("t")) {
            Serial.println("\n>>> Zeroing scale... please do not touch...");
            scale.tare(30);
            for (int i = 0; i < FILTER_SIZE; i++) weightBuffer[i] = 0.0f;
            bufferFilled = false;
            bufferIndex = 0;
            Serial.println(">>> [TARE COMPLETE] Scale reset to 0.00 g <<<\n");
        }
    }
}
