#!/usr/bin/env python3
import time
import smbus
import RPi.GPIO as GPIO
from datetime import datetime
import requests

# ─────────────────────────────
# Stark/Friend Hybrid Firebase Config
# ─────────────────────────────
# We are targeting your database, but making a new folder for YOUR sensor
FIREBASE_URL = "https://smartassistivesystem-39072-default-rtdb.firebaseio.com/sensors/Tsl2591.json"
DEVICE_ID = "Tsl2591_Lux_Module"

LED_PIN = 18
LOG_INTERVAL = 1    # seconds

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(LED_PIN, GPIO.OUT)

bus = smbus.SMBus(1)
ADDR = 0x29

# Enable sensor
try:
    bus.write_byte_data(ADDR, 0x80, 0x03)
except:
    pass

time.sleep(0.8)
print(" TSL2591 WORKING - Lux changing now. Initiating REST API uplink...\n")

last_log_time = time.time()
log_count = 1

try:
    while True:
        try:
            data = bus.read_i2c_block_data(ADDR, 0x94, 4)
            ch0 = data[1] << 8 | data[0]
            ch1 = data[3] << 8 | data[2]
            lux = ch0 - ch1
            if lux < 0:
                lux = 0
        except:
            lux = 0

        # ===== LOGGING & REST API UPLOAD =====
        current_time = time.time()
        if current_time - last_log_time >= LOG_INTERVAL:
            timestamp = datetime.now().strftime("%H:%M:%S")
            print(f"[{log_count}] {timestamp} | Lux = {lux}")
            
            # Build the payload using your friend's blueprint
            payload = {
                "timestamp": datetime.utcnow().isoformat() + "Z",
                "deviceId": DEVICE_ID,
                "lux_value": lux
            }

            # Fire the data at the database (using PUT to overwrite the live value)
            try:
                resp = requests.put(FIREBASE_URL, json=payload, timeout=5)
                if resp.status_code == 200:
                    print("✅ Telemetry uploaded successfully.")
                else:
                    print(f"❌ Firebase rejected the payload: {resp.status_code}")
            except Exception as e:
                print(f"❌ Comms failure: {e}")

            log_count += 1
            last_log_time = current_time

        # ===== LED CONTROL =====
        if lux < 100:
            GPIO.output(LED_PIN, GPIO.HIGH)
        else:
            GPIO.output(LED_PIN, GPIO.HIGH)
            time.sleep(0.2 + lux / 5000)
            GPIO.output(LED_PIN, GPIO.LOW)
            time.sleep(0.2 + lux / 5000)

        time.sleep(0.05)

except KeyboardInterrupt:
    GPIO.cleanup()
    print("\n Good test.")
