import time
import smbus

# ---------- TCS34725 CONSTANTS ----------
TCS34725_ADDRESS      = 0x29
TCS34725_COMMAND_BIT  = 0x80

TCS34725_ENABLE       = 0x00
TCS34725_ATIME        = 0x01
TCS34725_CONTROL      = 0x0F

TCS34725_ID           = 0x12
TCS34725_STATUS       = 0x13

TCS34725_CDATAL       = 0x14  # Clear channel data low byte
TCS34725_RDATAL       = 0x16  # Red
TCS34725_GDATAL       = 0x18  # Green
TCS34725_BDATAL       = 0x1A  # Blue

# ENABLE register bits
TCS34725_ENABLE_PON   = 0x01  # Power ON
TCS34725_ENABLE_AEN   = 0x02  # ADC Enable

# ---------- I2C BUS (use /dev/i2c-1) ----------
# IMPORTANT: tari Pi par /dev/i2c-1 che, etle bus = 1
bus = smbus.SMBus(1)


def write8(reg, value):
    bus.write_byte_data(TCS34725_ADDRESS, TCS34725_COMMAND_BIT | reg, value)


def read8(reg):
    return bus.read_byte_data(TCS34725_ADDRESS, TCS34725_COMMAND_BIT | reg)


def read16(reg):
    # TCS34725 returns LSB first (little-endian)
    low = bus.read_byte_data(TCS34725_ADDRESS, TCS34725_COMMAND_BIT | reg)
    high = bus.read_byte_data(TCS34725_ADDRESS, TCS34725_COMMAND_BIT | (reg + 1))
    return (high << 8) | low


def tcs_init():
    sensor_id = read8(TCS34725_ID)
    print("Sensor ID:", hex(sensor_id))

    # Integration time set (0xEB ~ 100ms)
    write8(TCS34725_ATIME, 0xEB)

    # Gain set (0x01 = 4x)
    write8(TCS34725_CONTROL, 0x01)

    # Enable: Power ON
    write8(TCS34725_ENABLE, TCS34725_ENABLE_PON)
    time.sleep(0.01)

    # Enable: Power ON + ADC (start conversions)
    write8(TCS34725_ENABLE, TCS34725_ENABLE_PON | TCS34725_ENABLE_AEN)
    time.sleep(0.7)  # Wait for first integration cycle


def read_color():
    c = read16(TCS34725_CDATAL)
    r = read16(TCS34725_RDATAL)
    g = read16(TCS34725_GDATAL)
    b = read16(TCS34725_BDATAL)
    return r, g, b, c


def classify_color(r, g, b):
    if r > g and r > b:
        return "RED"
    elif g > r and g > b:
        return "GREEN"
    elif b > r and b > g:
        return "BLUE"
    else:
        return "MIXED / UNKNOWN"


def main():
    print("Initializing TCS34725 on Raspberry Pi (I2C bus 1)...")
    tcs_init()

    while True:
        r, g, b, c = read_color()
        detected = classify_color(r, g, b)

        print(f"R: {r}, G: {g}, B: {b}, Clear: {c}")
        print("Detected Color:", detected)
        print("-" * 40)

        time.sleep(0.5)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nExiting...")
