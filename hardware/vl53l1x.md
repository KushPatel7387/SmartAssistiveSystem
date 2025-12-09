
# Build instructions for vl53l1x



![Photo](https://github.com/PrototypeZone/ceng317/blob/main/hardware/projects/media/vl53l1x.jpg)



The target audience is a CENG student from any post-secondary institution who would like to recreate this project using the VL53L1X Time-of-Flight (ToF) distance sensor. This guide explains the materials, software setup, code examples, troubleshooting, and future work needed to integrate the VL53L1X with a Raspberry Pi. 



## Materials

Every CENG 317 student already has (from earlier semesters and course kits):

Raspberry Pi Extreme Kit (Pi, power supply, case, etc.)

SD card + reader

Sense HAT

USB network adapter + Ethernet cable

2N3904 / 2N4124 NPN transistor

220 Ω and 2.2 kΩ resistors

5 mm red LED

Stacking header for Pi HAT

Grove/Qwiic connectors and cables

16 mm standoffs + M2.5 screws

3 mm acrylic sheets for laser cutting

Lead-free solder, tools, safety glasses


[Order parts from Digikey](https://github.com/PrototypeZone/ceng317/blob/main/hardware/digikeyorder.md)  

[Order PCB](https://github.com/PrototypeZone/ceng317/tree/main/hardware/pcb)  

[Order plastic case](https://github.com/PrototypeZone/ceng317/tree/main/hardware/lasercutting)  



[Soldering](https://github.com/PrototypeZone/ceng317/blob/main/hardware/pcb/inspection.md)  

[Raspberrypi Image](https://github.com/PrototypeZone/ceng153/blob/main/image.md)  

Additional parts for this project

1 × VL53L1X Time-of-Flight distance sensor breakout
(Typical I²C address: 0x29)

1 × Qwiic cable or Qwiic-to-wires adapter

Optional: acrylic cut-out or window for unobstructed forward-facing distance sensing



## Software



Required system packages:  

sudo apt update
sudo apt install python3.9 python3.9-distutils



 [Python code](vl53l1x.py)   



## Troubleshooting



[Generic](https://github.com/PrototypeZone/ceng317/blob/main/hardware/troubleshooting.md)   

plus any student/project specific comments:   



## Future work

[empyrebase integration](https://github.com/emrothenberg/empyrebase)  
