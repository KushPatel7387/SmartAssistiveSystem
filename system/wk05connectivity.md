# Hardware Connectivity Status Update   

## Recent and current progress   
Student D (Sarang) focused on establishing the full data pipeline: Sensor → Raspberry Pi → Firebase → Android App.   

**Connectivity & Firebase:**   
The Raspberry Pi has been configured to auto-connect to the Humber campus Wi-Fi. I have verified that the connection remains stable during lab sessions. I implemented a Python script using the `firebase-admin` (or `pyrebase`) library to push sensor data to the Realtime Database. The latency between a change in sensor environment and the update appearing on the Firebase console is minimal (< 1 second), confirming the network approach is viable.   

**I2C Pull-Up Resistor Status:**
We verified the schematics and physical PCBs for our sensors (TSL2591, VL53L1X, TCS34725, AS7262).   
* **Status:** All four breakout boards contain built-in pull-up resistors (typically 4.7kΩ or 10kΩ) on the SDA and SCL lines.   
* **Disconnection method:** These resistors are enabled by default. However, they **can** be disconnected by cutting the small PCB trace connecting the "PU" or "I2C" jumper pads on the back of the boards.   
* **Current State:** For individual testing, we have left them connected. For the final integration, if the parallel resistance drops too low (impeding the bus), we will cut the traces on all but one device or rely on the I2C Multiplexer to handle bus isolation.   

## Problems and hyperlinks/urls to potential solutions   
**Issue:** I2C Address Conflict   
Three of our sensors (TSL2591, VL53L1X, TCS34725) default to I2C address **0x29**. They cannot operate on the same bus simultaneously without hardware conflict.   

**Solutions:**   
1.  **I2C Multiplexer (Selected Solution):** We will use a TCA9548A to split the I2C bus into separate channels. This allows us to talk to multiple 0x29 devices by programmatically switching channels.   
    * [TCA9548A Datasheet & Product](https://www.ti.com/product/TCA9548A)   
2.  **Software Handling:**   
    * For Firebase communication on the Pi: [Pyrebase GitHub](https://github.com/thisbejim/Pyrebase) or [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)      

## Financial   
### Expenditures since previous report   
No new hardware purchases were made this week. Development utilized the existing parts kit and previously purchased sensors.   
**Total: $0**   

### Planned future expenses
* **I2C Multiplexer (TCA9548A):** ~$10.00 - $15.00 CAD (Essential for resolving the 0x29 address conflict).
* **Proto-board/Wires:** ~$5.00 CAD for finalizing connections.
