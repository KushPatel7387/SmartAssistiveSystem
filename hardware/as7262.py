import time
import board
import busio
import adafruit_as726x
import requests
from datetime import datetime

# ─────────────────────────────
# Firebase config
# ─────────────────────────────
FIREBASE_URL = "https://smartassistivesystem-39072-default-rtdb.firebaseio.com"

# We will overwrite this exact node each time:
# /sensors/As726x
SENSOR_NODE_PATH = "/sensors/As726x.json"

DEVICE_ID = "As726x"   # optional, just stored in the payload

# ─────────────────────────────
# AS726x setup
# ─────────────────────────────
i2c = busio.I2C(board.SCL, board.SDA)
sensor = adafruit_as726x.AS726x_I2C(i2c)

try:
    sensor.gain = 64  # valid values: 1, 3.7, 16, 64
except (AttributeError, ValueError):
    pass

try:
    sensor.integration_time = 50  # 0–255
except (AttributeError, ValueError):
    pass

print("AS726x → Firebase (/sensors/As726x, overwrite) started")

channel_names = ["Violet", "Blue", "Green", "Yellow", "Orange", "Red"]

while True:
    # Turn on driver LED (if supported)
    try:
        sensor.driver_led = True
    except AttributeError:
        pass

    # Start measurement (if supported)
    try:
        sensor.start_measurement()
    except AttributeError:
        pass

    # Wait for data
    try:
        while not sensor.data_ready:
            time.sleep(0.01)
    except AttributeError:
        time.sleep(0.25)

    # Read spectral values
    try:
        violet = sensor.violet
        blue = sensor.blue
        green = sensor.green
        yellow = sensor.yellow
        orange = sensor.orange
        red = sensor.red
    except AttributeError as e:
        print("Error reading spectral values:", e)
        break

    try:
        temperature = sensor.temperature
    except AttributeError:
        temperature = None

    # Turn off LED
    try:
        sensor.driver_led = False
    except AttributeError:
        pass

    # Build payload
    payload = {
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "deviceId": DEVICE_ID,
        "violet": violet,
        "blue": blue,
        "green": green,
        "yellow": yellow,
        "orange": orange,
        "red": red,
        "temperature": temperature,
    }

    # Print locally
    print("---- New Reading (overwriting /sensors/As726x) ----")
    for name, val in zip(channel_names,
                         [violet, blue, green, yellow, orange, red]):
        print(f"{name}: {val:.2f}")
    print("Temperature:", temperature)

    # 🔁 Overwrite /sensors/As726x using PUT
    try:
        resp = requests.put(FIREBASE_URL + SENSOR_NODE_PATH,
                            json=payload, timeout=5)
        if resp.status_code == 200:
            print("✅ Current reading saved at /sensors/As726x")
        else:
            print("❌ Firebase error:", resp.status_code, resp.text)
    except Exception as e:
        print("❌ Network/Request error:", e)

    print()
    time.sleep(1)   # change frequency if you want
