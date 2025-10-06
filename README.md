# 🧠 SmartAssistiveSystem
### Software Project – Group 5

## 📌 Overview
**SmartAssistiveSystem** is a smart assistive device designed to help **visually impaired people navigate safely**.  
It integrates multiple sensors with a **Raspberry Pi** to detect obstacles, light, color, and environmental conditions, providing **real-time voice or vibration feedback** for better awareness and mobility.

---

## 🛠️ Hardware Components
- **TSL2591** – Light sensor
- **TCS34725** – Color sensor
- **VL53L1X** – Distance/Obstacle sensor
- **AS7262 / AS7341** – Spectrum sensors
- **Raspberry Pi** – Central controller for data processing and feedback

---

## 🚀 Features

### 🧱 Obstacle Detection
Detects nearby objects using the **VL53L1X Time-of-Flight sensor** and warns the user through vibration or voice feedback.  
Helps prevent collisions and ensures safe movement in real time.

### 💡 Light & Color Detection
Measures ambient light using the **TSL2591** and detects object colors via the **TCS34725**.  
Provides voice alerts about light conditions and identified colors (e.g., “Red detected” or “Low light”).

### 🌈 Environment Awareness (Spectrum Sensors)
Uses **AS7262 / AS7341** to analyze light spectra and identify different surroundings or materials.  
Improves environmental understanding, especially outdoors or in changing light conditions.

### 🔊 Real-Time Feedback (Voice/Vibration)
Delivers instant responses to sensor inputs through **text-to-speech audio** or **haptic vibration**.  
Ensures users receive continuous and accessible feedback for confident navigation.

---

## ⚙️ System Workflow
**Sensors → Raspberry Pi → Data Processing → Voice/Vibration Feedback**

Each sensor continuously gathers data and sends it to the Raspberry Pi.  
The system interprets the readings and alerts the user instantly — through sound cues or vibration patterns — to enhance environmental awareness.

---

## 👥 Team – Group 5
- **Kush Patel** - n0657387
- **Daksh Rana** - n01664095
- **Krish Patel** - n01666556
- **Sarang Prajapati** - n01662036

---

## 🧩 Summary
SmartAssistiveSystem combines **IoT sensors, Raspberry Pi processing, and assistive feedback** to provide a reliable companion for visually impaired individuals.  
It makes real-world navigation **smarter, safer, and more independent**.

---
