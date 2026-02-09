# Hardware Connectivity Status Update

## Recent and current progress
This week the group focused on verifying end-to-end hardware connectivity between the Raspberry Pi, sensors, campus Wi-Fi, and Firebase. The Raspberry Pi successfully connects to Humber campus Wi-Fi and maintains a stable internet connection during lab sessions. Firebase Realtime Database read/write operations were tested from the Raspberry Pi using Python scripts, confirming reliable data upload for all active sensors.

Local I2C communication between the Raspberry Pi and sensors (TSL2591, VL53L1X, TCS34725, AS7262) was verified using i2cdetect and individual test scripts. Sensor values are correctly captured and formatted before being sent to Firebase. Initial latency testing shows acceptable response times for real-time monitoring in the Android application.

Preparation for the interim demo is underway, with focus on stability rather than feature expansion.

## Problems and hyperlinks/urls to potential solutions
**Issue:** Multiple sensors share the same I2C address (0x29), which prevents simultaneous operation on a single I2C bus without isolation.

**Current mitigation:** Sensors are tested individually during development. For full integration, an I2C multiplexer will be required.

**Potential solutions:**
- I2C multiplexer (TCA9548A):  
  https://www.ti.com/product/TCA9548A
- Raspberry Pi ↔ Arduino USB serial communication (if offloading sensors):  
  https://roboticsbackend.com/raspberry-pi-arduino-serial-communication/
- Firebase Python libraries for Raspberry Pi (pyrebase / empyrebase):  
  https://github.com/thisbejim/Pyrebase

Campus Wi-Fi occasionally blocks rapid reconnect attempts after power cycling the Raspberry Pi. This is mitigated by allowing sufficient delay between reconnect attempts and using saved network profiles.

## Financial
### Expenditures since previous report
No new hardware purchases were made this week. All testing was performed using previously purchased sensors, Raspberry Pi hardware, and existing cables.

Cost impact this week: **$0**

### Planned future expenses
- I2C multiplexer (TCA9548A or equivalent): **~$10–$20 CAD**
- Additional Qwiic cables or jumper wires (if required): **~$5–$10 CAD**
<img width="2035" height="1218" alt="image" src="https://github.com/user-attachments/assets/46cc5ef1-d4fb-4264-80a3-c4847258a4d8" />
