# 👁️ SmartAssistiveSystem
### Software Project – Group 5

## 📌 Overview
SmartAssistiveSystem is an Android + IoT solution designed to help **visually impaired individuals** navigate safely.  
It uses sensors with a **Raspberry Pi** to detect **obstacles, light, color, and surroundings**, providing **real-time feedback** through sound or vibration.

---

## 🛠️ Hardware
- **TSL2591** – Light Sensor
- **TCS34725** – Color Sensor
- **VL53L1X** – Distance Sensor
- **AS7262 / AS7341** – Spectrum Sensor
- **Raspberry Pi / ESP32** – Main Controller

---

## 💡 Key Features
- Obstacle and distance detection
- Light and color recognition
- Real-time alerts via audio or vibration
- GPS and emergency alert function
- Firebase cloud integration
- English & French language support

---

## 🧩 System Integration
All sensors connect to the controller via **I²C/SPI**, and data is sent to the Android app through **Bluetooth/Wi-Fi**.  
The app displays live readings, triggers alerts, and syncs data to **Firebase** for caretaker monitoring.

---

## 🎯 Project Scope
Develop a **Smart Assistive System** combining hardware sensors and a mobile app to improve navigation safety.  
The project covers hardware integration, Android app development, and cloud connectivity.  
It’s complete when all sensors work accurately, alerts function in real time, and the app runs smoothly.

---

## 📅 Timeline
| Phase                          | Duration        | Focus                       |
|--------------------------------|-----------------|-----------------------------|
| 1. Kickoff & Requirements      | Sept 26 – Oct 2 | Project planning            |
| 2. GitHub Setup & Architecture | Sept 29 – Oct 6 | Repo & design setup         |
| 3. App UI & Navigation         | Oct 3 – Oct 12  | Splash, Drawer, Settings    |
| 4. Sensor Integration          | Oct 10 – Oct 22 | AS7262, AS7341, TCS34725    |
| 5. Integration & Testing       | Oct 18 – Nov 10 | Full system test            |
| 6. Documentation & Review      | Nov 15 – Dec 15 | Final report & presentation |

---

## 👥 Team – Group 5
| Name                 | ID        | Role                 |
|----------------------|-----------|----------------------|
| **Kush Patel**       | N01657387 | Android Development  |
| **Daksh Rana**       | N01664095 | Database & Firebase  |
| **Krish Patel**      | N01666556 | Architecture & Cloud |
| **Sarang Prajapati** | N01662036 | Sensor Integration   |

---

## 🔗 Links
- **GitHub Repo:** [SmartAssistiveSystem](https://github.com/KushPatel7387/SmartAssistiveSystem)
- 
## Currenlty working on Deliverable 5


## Screenshot of the Application
![App Home Screen](https://github.com/KushPatel7387/SmartAssistiveSystem/blob/master/application_photos/HomeScreen.jpg?raw=true)
![Settings Fragment](https://github.com/KushPatel7387/SmartAssistiveSystem/blob/master/application_photos/Settings.jpg?raw=true)
![Sensor Readings](https://github.com/KushPatel7387/SmartAssistiveSystem/blob/master/application_photos/Sensors.jpg?raw=true)
![Feedback Fragment](https://github.com/KushPatel7387/SmartAssistiveSystem/blob/master/application_photos/Feedback.jpg?raw=true)
![Patient List](https://github.com/KushPatel7387/SmartAssistiveSystem/blob/master/application_photos/Patients.jpg?raw=true)

© 2025 **SmartAssistiveSystem – Group 5**  
*CENG-322 Software Project, Humber College*
