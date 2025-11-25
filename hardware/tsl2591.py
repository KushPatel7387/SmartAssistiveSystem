#!/usr/bin/env python3
import time
import smbus
import RPi.GPIO as GPIO

LED_PIN = 18
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(LED_PIN, GPIO.OUT)

bus = smbus.SMBus(1)
ADDR = 0x29

try:
    bus.write_byte_data(ADDR, 0x80, 0x03)
except:
    pass
time.sleep(0.8)

print("TSL2591 WORKING - Lux changing now")

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

        print(f"Lux: {lux:6}", end="\r")

        if lux < 100:
            GPIO.output(LED_PIN, GPIO.HIGH)
        else:
            GPIO.output(LED_PIN, GPIO.HIGH)
            time.sleep(0.2 + lux/5000)
            GPIO.output(LED_PIN, GPIO.LOW)
            time.sleep(0.2 + lux/5000)

        time.sleep(0.1)

except KeyboardInterrupt:
    GPIO.cleanup()
    print("\nStep 2 & 3 COMPLETE")
