# CENG 355: Week 11 Troubleshooting

## Problems

### 1. Critical Component Delay (Resolved)
**Description:** For the duration of the week leading up to the deadline, the **I2C Multiplexer (MUX)** was delayed in transit. This created a hardware roadblock because three of our sensors share conflicting I2C addresses, preventing them from communicating on the same bus:
* **VL53L1X** (Distance Sensor)
* **TSL2591** (Light Sensor) 
* **TCS34725** (Color Sensor)

**Resolution:** The component arrived on **Monday, April 6th**, just before the start of class, allowing us to move forward with the primary hardware design.

---

## Solutions

### 1. Contingency Planning (Pre-Arrival)
Before the component arrived, the following backup strategies were developed to ensure the project remained viable:

* **Software Address Reconfiguration:** Planning to use the VL53L1X's capability to change its I2C address via software during the boot sequence to avoid conflicts.
* **Dual-Controller Setup:** Temporary architectural pivot to use two Raspberry Pis to manage the sensors on separate buses.
* **Emergency Re-order:** Prepared to source a local replacement in Toronto if the delay extended past Monday morning.

### 2. Current Status & Timeline
**Status:** `Active Integration`

**Current Task:** Now that the hardware is physically present, we are abandoning the Dual-Pi workaround and proceeding with the primary design using the I2C MUX.

**Timeline:**
* **April 6th (Lab):** Physical hardware wiring for the MUX is being completed.
* **April 7th - 9th:** Implementation of the I2C control logic and verification of sensor data integrity.

---

## Weekly Checklist
- [x] Present in person in a safe manner (Safety glasses, no food/drink).
- [x] Hardware and project equipment present.
- [x] Up to date `system/wk10bom.md` available in repo.
- [x] Problems and Solutions identified in this document.
