/**
 * ============================================================================
 * TEST 03A: STANDALONE BUZZER TEST (ESP32)
 * ============================================================================
 * Hardware Pin: GPIO 26
 * Onboard LED:  GPIO 2 (Used as visual confirmation)
 * 
 * DIRECT CONNECTIONS (NO BREADBOARD REQUIRED):
 * -------------------------------------------------------------
 * IF 2-PIN BUZZER:
 *   - Positive (+) / Long leg  --> Female-to-Female wire --> ESP32 GPIO 26
 *   - Negative (-) / Short leg --> Female-to-Female wire --> ESP32 GND
 * 
 * IF 3-PIN BUZZER MODULE (PCB with 3 pins):
 *   - VCC / (+)               --> Female-to-Female wire --> ESP32 3.3V or 5V (VIN)
 *   - GND / (-)               --> Female-to-Female wire --> ESP32 GND
 *   - I/O / S / SIG           --> Female-to-Female wire --> ESP32 GPIO 26
 * ============================================================================
 */

#define BUZZER_PIN    26
#define ONBOARD_LED   2   // ESP32 onboard blue LED for visual sync

// Helper function to generate tone for PASSIVE buzzers
void playTone(int frequency, int durationMs) {
    int periodUs = 1000000 / frequency;
    int halfUs = periodUs / 2;
    long cycles = ((long)frequency * durationMs) / 1000;

    for (long i = 0; i < cycles; i++) {
        digitalWrite(BUZZER_PIN, HIGH);
        delayMicroseconds(halfUs);
        digitalWrite(BUZZER_PIN, LOW);
        delayMicroseconds(halfUs);
    }
}

void setup() {
    Serial.begin(115200);
    delay(1000);

    pinMode(BUZZER_PIN, OUTPUT);
    digitalWrite(BUZZER_PIN, LOW);

    pinMode(ONBOARD_LED, OUTPUT);
    digitalWrite(ONBOARD_LED, LOW);

    Serial.println("\n==================================================");
    Serial.println("         ESP32 STANDALONE BUZZER TEST");
    Serial.println("==================================================");
    Serial.println("Pin Config: BUZZER = GPIO 26 | LED = GPIO 2");
    Serial.println("Note: The blue onboard LED will blink in sync.");
    Serial.println("Starting test routine in 2 seconds...\n");
    delay(2000);
}

void loop() {
    // ---------------------------------------------------------
    // TEST 1: Active Buzzer DC Pulse (Standard HIGH / LOW)
    // ---------------------------------------------------------
    Serial.println("[TEST 1] Active Buzzer DC Pulse: 3 Medium Beeps...");
    for (int i = 0; i < 3; i++) {
        Serial.print("  -> BEEP "); Serial.println(i + 1);
        digitalWrite(BUZZER_PIN, HIGH);
        digitalWrite(ONBOARD_LED, HIGH);
        delay(300);
        digitalWrite(BUZZER_PIN, LOW);
        digitalWrite(ONBOARD_LED, LOW);
        delay(200);
    }
    delay(1000);

    // ---------------------------------------------------------
    // TEST 2: Rapid Alert Chime (Active & Passive compatible)
    // ---------------------------------------------------------
    Serial.println("[TEST 2] Rapid Pulses: 5 Fast Beeps...");
    for (int i = 0; i < 5; i++) {
        digitalWrite(BUZZER_PIN, HIGH);
        digitalWrite(ONBOARD_LED, HIGH);
        delay(80);
        digitalWrite(BUZZER_PIN, LOW);
        digitalWrite(ONBOARD_LED, LOW);
        delay(80);
    }
    delay(1000);

    // ---------------------------------------------------------
    // TEST 3: Multi-Frequency Tone Sweep (Ideal for Passive Buzzer)
    // ---------------------------------------------------------
    Serial.println("[TEST 3] Tone Frequency Sweep (2000Hz, 2500Hz, 3000Hz)...");
    digitalWrite(ONBOARD_LED, HIGH);
    playTone(2000, 200);
    delay(50);
    playTone(2500, 200);
    delay(50);
    playTone(3000, 300);
    digitalWrite(ONBOARD_LED, LOW);

    Serial.println("\n--- Cycle Complete. Pausing 3 seconds before next cycle ---\n");
    delay(3000);
}
