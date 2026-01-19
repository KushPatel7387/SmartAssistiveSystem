# Hardware Status Update

## Recent and current progress
Group 5 (Krish, Sarang, Daksh, Kush) demonstrated the hardware to the professor. All hardware and sensors are working. Krish, Daksh, and Kush worked on the first try. Sarang initially forgot the username/password, reset it, and then successfully demonstrated. Week 2 demo confirmed all sensors work fine. Group 5 demo is done.

## Problems and hyperlinks/urls to potential solutions
**Problem:** Krish, Daksh, and Kush sensors share the same I2C address **0x29**, while Sarang’s is **0x49**. Same-address devices cannot work together on the same I2C bus unless we separate them.

**Potential solutions:**
- **Use an I2C multiplexer (recommended):** e.g., **TCA9548A** to put each 0x29 sensor on a different channel.
  - https://www.ti.com/product/TCA9548A
- **If the 0x29 sensor supports address change:** use shutdown/XSHUT control to power up sensors one-by-one and reassign addresses (only if supported by that sensor model).

## Financial
### Expenditures since previous report
- DigiKey group order (with sensor order): **$92.78 USD**
- Sensor costs (per student):
  - Krish: **$20 CAD**
  - Sarang: **$47 CAD**
  - Daksh: **$25 CAD**
  - Kush: **$35 CAD**

### Planned future expenses
- Purchase **I2C multiplexer** (TCA9548A or equivalent) to resolve the **0x29** address conflict.
