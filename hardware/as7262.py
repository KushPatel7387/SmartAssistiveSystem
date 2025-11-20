import time
import board
import busio
import adafruit_as726x

# Create I2C bus
i2c = busio.I2C(board.SCL, board.SDA)

# Create sensor object
sensor = adafruit_as726x.AS726x_I2C(i2c)

# Optional: gain + integration time
try:
    # Allowed gain values: 1, 3.7, 16, 64
    sensor.gain = 64          # use 1 if this saturates
except (AttributeError, ValueError):
    pass

try:
    # In milliseconds (2.8–714 ms depending on value)
    sensor.integration_time = 50
except (AttributeError, ValueError):
    pass

print("AS726x Spectral Sensor Test")

channel_names = ["Violet", "Blue", "Green", "Yellow", "Orange", "Red"]

while True:
    # Turn on driver LED (flash LED on the board), if supported
    try:
        sensor.driver_led = True
    except AttributeError:
        pass

    # Start a new measurement, if supported
    try:
        sensor.start_measurement()
    except AttributeError:
        pass

    # Wait until data is ready (or just sleep if property not present)
    try:
        while not sensor.data_ready:
            time.sleep(0.01)
    except AttributeError:
        time.sleep(0.25)

    # Read calibrated values from individual properties
    try:
        values = [
            sensor.violet,
            sensor.blue,
            sensor.green,
            sensor.yellow,
            sensor.orange,
            sensor.red,
        ]
    except AttributeError as e:
        print("Error reading spectral values:", e)
        break

    print("---- New Reading ----")
    for name, val in zip(channel_names, values):
        print(f"{name}: {val:.2f}")

    # On-chip temperature (if available)
    try:
        print(f"Temperature: {sensor.temperature:.1f} °C")
    except AttributeError:
        pass

    # Turn off LED between reads
    try:
        sensor.driver_led = False
    except AttributeError:
        pass

    print()
    time.sleep(1)
