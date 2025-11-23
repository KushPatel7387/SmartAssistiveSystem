import time
import board
import busio
from adafruit_vl53l1x import VL53L1X

print("Adafruit VL65L1X sensor demo")

i2c = busio.I2C(board.SCL, board.SDA)

vl65 = VL53L1X(i2c)

vl65.distance_mode = 2      # long distance
vl65.timing_budget = 50     # ms

print("VL65L1X sensor initialized!")
print("Ranging started")
vl65.start_ranging()

time.sleep(0.2)   # give sensor time to start

while True:
    try:
        distance = vl65.distance   # returns mm

        if distance is None:
            print("Distance: --- mm")
 else:
            print("Distance:", int(distance), "mm")

        time.sleep(0.05)

    except KeyboardInterrupt:
        print("\nStopping...")
        vl65.stop_ranging()
        break
