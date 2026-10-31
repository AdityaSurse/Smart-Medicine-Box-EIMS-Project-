/**
 * ============================================================================
 * TEST 03B: STANDALONE REED SWITCH TEST (ESP32)
 * ============================================================================
 * Hardware Pin: GPIO 27 (Configured with internal INPUT_PULLUP)
 * Onboard LED:  GPIO 2  (Lights UP when magnet is detected / box closed)
 * 
 * DIRECT CONNECTIONS (NO BREADBOARD REQUIRED):
 * -------------------------------------------------------------
 * IF 2-WIRE REED SWITCH (Glass tube or Door Contact with wires):
 *   - Wire 1 (either wire) --> Female-to-Male/Female jumper --> ESP32 GPIO 27
 *   - Wire 2 (other wire)  --> Female-to-Male/Female jumper --> ESP32 GND
 *   *Note: Reed switches have NO POLARITY. Direction does not matter!
 *   *Note: NO external resistor is needed because ESP32 internal pullup is ON.
 * 
 * IF 3-PIN REED MODULE (PCB with potentiometer / chip):
 *   - VCC                  --> Female-to-Female jumper --> ESP32 3.3V
 *   - GND                  --> Female-to-Female jumper --> ESP32 GND
 *   - DO (Digital Output)  --> Female-to-Female jumper --> ESP32 GPIO 27
 * 
 * QUICK BENCHMARK TIP:
 *   If you don't have the magnet connected yet, take a single jumper wire,
 *   plug one end into GPIO 27, and touch the other end directly to ESP32 GND.
 *   The Serial Monitor should immediately show "CLOSED" and LED turns ON!
 * ============================================================================
 */

#define REED_PIN      27
#define ONBOARD_LED   2

int lastState = -1;
unsigned long lastHeartbeat = 0;

void setup() {
    Serial.begin(115200);
    delay(1000);

    // CRITICAL: INPUT_PULLUP holds GPIO 27 at 3.3V internally.
    // When the reed switch closes (magnet near), it shorts 27 to GND (reads 0 / LOW).
    pinMode(REED_PIN, INPUT_PULLUP);

    // Onboard blue LED for instant visual feedback
    pinMode(ONBOARD_LED, OUTPUT);
    digitalWrite(ONBOARD_LED, LOW);

    Serial.println("\n==================================================");
    Serial.println("       ESP32 STANDALONE REED SWITCH TEST");
    Serial.println("==================================================");
    Serial.println("Pin Config: REED = GPIO 27 (INPUT_PULLUP) | LED = GPIO 2");
    Serial.println("Logic:");
    Serial.println("  - Pin 27 = 0 (LOW)  --> MAGNET NEAR / LID CLOSED (LED ON)");
    Serial.println("  - Pin 27 = 1 (HIGH) --> MAGNET AWAY / LID OPEN   (LED OFF)");
    Serial.println("--------------------------------------------------");
    Serial.println("Bring the magnet close to the switch to test!\n");
}

void loop() {
    int currentState = digitalRead(REED_PIN);

    // 1. Report only when the state changes (Instant event detection)
    if (currentState != lastState) {
        lastState = currentState;

        if (currentState == LOW) {
            // Magnet is close -> Switch contacts closed -> Pin connected to GND
            digitalWrite(ONBOARD_LED, HIGH);
            Serial.println(">>>>> [EVENT] MAGNET DETECTED! <<<<<");
            Serial.println("      State: CLOSED (Lid Shut)");
            Serial.println("      Pin 27 Level: 0 (LOW / GND)");
            Serial.println("      Onboard LED: ON\n");
        } else {
            // Magnet is away -> Switch contacts open -> Pin pulled HIGH to 3.3V
            digitalWrite(ONBOARD_LED, LOW);
            Serial.println(">>>>> [EVENT] MAGNET REMOVED! <<<<<");
            Serial.println("      State: OPEN (Lid Opened)");
            Serial.println("      Pin 27 Level: 1 (HIGH / 3.3V)");
            Serial.println("      Onboard LED: OFF\n");
        }
    }

    // 2. Periodic Heartbeat every 2 seconds so you know the code is running
    if (millis() - lastHeartbeat >= 2000) {
        lastHeartbeat = millis();
        Serial.print("[Heartbeat] Monitoring Pin 27... Current raw value: ");
        Serial.print(currentState);
        Serial.print(" (");
        Serial.print(currentState == LOW ? "CLOSED / MAGNET NEAR" : "OPEN / NO MAGNET");
        Serial.println(")");
    }

    delay(50); // Small debounce delay
}
