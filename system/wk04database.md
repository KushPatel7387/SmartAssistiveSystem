# Database Status Update
## Recent and current progress
Integrated Firebase Realtime Database (RTDB) for SmartAssistiveSystem and confirmed stable real-time read/write.
Main nodes working: Feedback, analytics/events, sensors, users.
Sensors logging (4 sensors)
AS726x (Spectral): red/orange/yellow/green/blue/violet + temperature + timestamp
TCS34725 (Color): r/g/b + color name + timestamp
VL53L1X (Distance): distance_mm + timestamp
TSL2591 (Light): light readings stored with timestamp
Analytics/events: stores screen, type (screen_view), userId, timestamp.
Feedback: stores comment, rating, name, email, phone, timestamp.
Users: structured as users/<userId>/ with subnodes like alerts, location, patients.
## Problems and hyperlinks/urls to potential solutions
Security risk: current rules are open (.read/.write = true) for users/feedback/sensors → privacy + spam risk.
Analytics access issue: analytics should be internal-only, but .read is currently allowed.
Data consistency: mixed timestamp formats (epoch vs ISO) may affect sorting/filtering.
Performance: sensor logs can grow large → need limits/pagination.
Links:
RTDB Security Rules:
https://firebase.google.com/docs/database/security
Auth-based rules examples:
https://firebase.google.com/docs/database/security/user-security
Read/Write (Android):
https://firebase.google.com/docs/database/android/read-and-write
Lists + limit/pagination:
https://firebase.google.com/docs/database/android/lists-of-data

## Financial
### Expenditures since previous report
Firebase Realtime Database (RTDB): using free tier (Spark plan) → $0
Development tools: Android Studio, GitHub → $0
Hardware (4 sensors for the system):
Color sensor: TCS34725
Light sensor: TSL2591
Distance sensor: VL53L1X
Spectral sensor: AS726x / AS7262
Estimated total for the 4 sensors (Amazon.ca reference): $80.60 CAD

### Planned future expenses
Wires/connectors/breadboard (if required): ~$10–$25 CAD
Backup sensor unit(s) (optional): ~$10–$45 CAD depending on the sensor
Cloud usage scaling (optional): If read/write volume becomes large, Firebase bandwidth/storage may exceed free tier → variable cost
Optional project showcase costs: custom domain $15–$25/year (only if we publish a public demo website)
