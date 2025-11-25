import time
import board
import busio
from adafruit_vl53l1x import VL53L1X

print("Adafruit VL53L1X sensor demo")
i2c = busio.I2C(board.SCL, board.SDA)

vl53 = VL53L1X(i2c)

vl53.distance_mode = 2       
vl53.timing_budget = 50      

print("VL53L1X sensor initialized!")
print("Ranging started...")

vl53.start_ranging()
time.sleep(0.2) 

try:
    while True:
        distance = vl53.distance  

        if distance is None:
            print("Distance: --- mm")
        else:
            print("Distance:", int(distance), "mm")

        time.sleep(0.05)

except KeyboardInterrupt:
    print("\nStopping...")
    vl53.stop_ranging()
    print("Ranging stopped. Program ended.")
