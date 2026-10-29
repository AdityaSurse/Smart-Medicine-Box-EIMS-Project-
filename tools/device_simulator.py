#!/usr/bin/env python3
"""
Smart Medicine Box - ESP32 Hardware Simulator
Simulates the physical ESP32 microcontroller publishing telemetry and sensor events to Cloud Firestore.

Usage Examples:
  # Run live telemetry loop in mock/preview mode:
  python3 tools/device_simulator.py --uid user_eleanor_8820 --device-id DG-4081

  # Simulate an immediate dose event (lid opened + weight drop):
  python3 tools/device_simulator.py --uid user_eleanor_8820 --simulate-dose

  # Run against local Firestore Emulator:
  python3 tools/device_simulator.py --emulator localhost:8080 --uid user_eleanor_8820
"""

import argparse
import datetime
import json
import os
import random
import sys
import time
import urllib.error
import urllib.request

# ANSI terminal colors
GREEN = "\033[92m"
BLUE = "\033[94m"
YELLOW = "\033[93m"
CYAN = "\033[96m"
RED = "\033[91m"
BOLD = "\033[1m"
RESET = "\033[0m"


class DeviceSimulator:
    def __init__(self, uid: str, device_id: str, project_id: str, emulator: str = None, api_key: str = None):
        self.uid = uid
        self.device_id = device_id
        self.project_id = project_id
        self.emulator = emulator
        self.api_key = api_key

        # Simulated physical state
        self.temp_celsius = 21.4
        self.humidity_percent = 46.0
        self.weight_grams = 14.8
        self.box_open = False
        self.battery_percent = 84
        self.wifi_rssi = -62

    def get_firestore_url(self, path: str) -> str:
        """Constructs the Firestore REST API URL."""
        if self.emulator:
            return f"http://{self.emulator}/v1/projects/{self.project_id}/databases/(default)/documents/{path}"
        return f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents/{path}"

    def send_firestore_document(self, path: str, fields_dict: dict, is_patch: bool = True) -> bool:
        """Publishes document payload via Firestore REST API."""
        url = self.get_firestore_url(path)
        if self.api_key:
            url += f"?key={self.api_key}"

        # Convert python dict to Firestore REST format
        firestore_fields = {}
        for k, v in fields_dict.items():
            if isinstance(v, bool):
                firestore_fields[k] = {"booleanValue": v}
            elif isinstance(v, int):
                firestore_fields[k] = {"integerValue": str(v)}
            elif isinstance(v, float):
                firestore_fields[k] = {"doubleValue": v}
            elif isinstance(v, str):
                firestore_fields[k] = {"stringValue": v}
            elif isinstance(v, list):
                firestore_fields[k] = {"arrayValue": {"values": [{"stringValue": str(item)} for item in v]}}

        payload = {"fields": firestore_fields}
        data_bytes = json.dumps(payload).encode("utf-8")

        headers = {"Content-Type": "application/json"}
        req = urllib.request.Request(
            url,
            data=data_bytes,
            headers=headers,
            method="PATCH" if is_patch else "POST"
        )

        try:
            with urllib.request.urlopen(req, timeout=5) as response:
                return response.status in (200, 201)
        except urllib.error.URLError as e:
            # When running without active network/emulator credentials, log gracefully
            return False

    def publish_telemetry(self):
        """Generates realistic sensor noise and updates telemetry."""
        # Jitter sensor readings
        self.temp_celsius = round(max(18.0, min(32.0, self.temp_celsius + random.uniform(-0.15, 0.15))), 1)
        self.humidity_percent = round(max(30.0, min(70.0, self.humidity_percent + random.uniform(-0.2, 0.2))), 1)
        now_iso = datetime.datetime.now(datetime.timezone.utc).isoformat()

        doc_path = f"users/{self.uid}/devices/{self.device_id}/telemetry/latest"
        payload = {
            "temp": self.temp_celsius,
            "humidity": self.humidity_percent,
            "weightGrams": round(self.weight_grams, 2),
            "boxOpen": self.box_open,
            "rtcTime": now_iso,
            "ts": now_iso,
            "batteryPercent": self.battery_percent,
            "wifiRssi": self.wifi_rssi
        }

        success = self.send_firestore_document(doc_path, payload, is_patch=True)
        status_tag = f"{GREEN}[ONLINE/REST]{RESET}" if success else f"{YELLOW}[MOCK/LOCAL]{RESET}"

        print(
            f"{status_tag} {CYAN}{now_iso}{RESET} -> "
            f"Temp: {BOLD}{self.temp_celsius}°C{RESET} | "
            f"Humidity: {BOLD}{self.humidity_percent}%{RESET} | "
            f"Weight: {BOLD}{self.weight_grams:.2f}g{RESET} | "
            f"Box: {RED if self.box_open else GREEN}{'OPEN' if self.box_open else 'CLOSED'}{RESET} | "
            f"Battery: {self.battery_percent}%"
        )

    def trigger_dose_cycle(self, bay_label: str = "Bay 01", pill_weight: float = 0.55):
        """Simulates opening lid, extracting pill, and closing lid."""
        now_ts = int(time.time() * 1000)
        now_iso = datetime.datetime.now(datetime.timezone.utc).isoformat()

        print(f"\n{BOLD}{YELLOW}>>> SIMULATING PHYSICAL DOSE INTAKE EVENT <<<{RESET}")
        print(f"1. Patient opens box lid ({bay_label})...")
        self.box_open = True
        self.publish_telemetry()

        # Publish BOX_OPENED event
        open_evt_path = f"users/{self.uid}/devices/{self.device_id}/events/evt_open_{now_ts}"
        open_evt_payload = {
            "eventId": f"evt_open_{now_ts}",
            "eventType": "BOX_OPENED",
            "timestamp": now_iso,
            "rtcTime": now_iso,
            "deltaWeightGrams": 0.0,
            "currentWeightGrams": round(self.weight_grams, 2),
            "trayBay": bay_label,
            "details": f"Magnetic reed switch tripped open on {bay_label}"
        }
        self.send_firestore_document(open_evt_path, open_evt_payload, is_patch=True)
        print(f"   Event emitted: {GREEN}BOX_OPENED{RESET}")

        time.sleep(2.0)

        # Pill extracted
        print(f"2. Patient extracts 1 tablet (~{pill_weight:.2f}g) from load cell tray...")
        self.weight_grams = max(0.0, self.weight_grams - pill_weight)

        drop_ts = int(time.time() * 1000)
        drop_iso = datetime.datetime.now(datetime.timezone.utc).isoformat()
        drop_evt_path = f"users/{self.uid}/devices/{self.device_id}/events/evt_drop_{drop_ts}"
        drop_evt_payload = {
            "eventId": f"evt_drop_{drop_ts}",
            "eventType": "WEIGHT_DROP",
            "timestamp": drop_iso,
            "rtcTime": drop_iso,
            "deltaWeightGrams": round(-pill_weight, 2),
            "currentWeightGrams": round(self.weight_grams, 2),
            "trayBay": bay_label,
            "details": f"Load cell detected weight reduction of {pill_weight:.2f}g"
        }
        self.send_firestore_document(drop_evt_path, drop_evt_payload, is_patch=True)
        print(f"   Event emitted: {GREEN}WEIGHT_DROP (-{pill_weight:.2f}g){RESET}")

        time.sleep(1.5)

        print("3. Patient closes box lid...")
        self.box_open = False
        self.publish_telemetry()
        print(f"{GREEN}>>> Dose cycle complete. Weight now {self.weight_grams:.2f}g. <<<{RESET}\n")

    def run_telemetry_loop(self, interval_seconds: int = 5, loop_count: int = None):
        """Runs the continuous telemetry publication loop."""
        print(f"{BOLD}{BLUE}======================================================={RESET}")
        print(f"{BOLD}{BLUE} Smart Medicine Box - ESP32 Firmware Telemetry Simulator{RESET}")
        print(f" Target User:   {CYAN}{self.uid}{RESET}")
        print(f" Device Serial: {CYAN}{self.device_id}{RESET}")
        print(f" Cadence:       Every {interval_seconds}s")
        if self.emulator:
            print(f" Emulator:      {self.emulator}")
        print(f"{BOLD}{BLUE}======================================================={RESET}\n")

        iterations = 0
        try:
            while loop_count is None or iterations < loop_count:
                self.publish_telemetry()
                time.sleep(interval_seconds)
                iterations += 1
        except KeyboardInterrupt:
            print(f"\n{YELLOW}Simulator paused by user.{RESET}")


def main():
    parser = argparse.ArgumentParser(
        description="Smart Medicine Box ESP32 Hardware Simulator (Firestore)"
    )
    parser.add_argument("--uid", default="user_eleanor_8820", help="Firebase Auth UID")
    parser.add_argument("--device-id", default="DG-4081", help="Box Serial Number / Device ID")
    parser.add_argument("--project-id", default="smart-medbox-prod", help="Firebase Project ID")
    parser.add_argument("--emulator", default=None, help="Firestore emulator host:port (e.g. localhost:8080)")
    parser.add_argument("--api-key", default=None, help="Firebase Web API Key for direct REST writes")
    parser.add_argument("--interval", type=int, default=5, help="Telemetry interval in seconds")
    parser.add_argument("--count", type=int, default=None, help="Number of telemetry cycles (None for infinite)")
    parser.add_argument("--simulate-dose", action="store_true", help="Simulate an immediate physical dose intake")
    parser.add_argument("--bay", default="Bay 01", help="Compartment bay for dose simulation")

    args = parser.parse_args()

    simulator = DeviceSimulator(
        uid=args.uid,
        device_id=args.device_id,
        project_id=args.project_id,
        emulator=args.emulator,
        api_key=args.api_key
    )

    if args.simulate_dose:
        simulator.trigger_dose_cycle(bay_label=args.bay)
    else:
        simulator.run_telemetry_loop(interval_seconds=args.interval, loop_count=args.count)


if __name__ == "__main__":
    main()
