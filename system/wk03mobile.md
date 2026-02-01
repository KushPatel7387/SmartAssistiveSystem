# Mobile Status Update
## Recent and current progress
Group 5 (Krish, Sarang, Daksh, Kush) continued development and verification of the Smart Assistive System Android application. The mobile app architecture remains stable and organized with clear separation between UI components, business logic, and utility modules, allowing future expansion as additional sensors are integrated.

This week the group verified correct operation of the following mobile features:

Login system supporting email and Google authentication
Home dashboard navigation
Alerts module for monitoring system events
Patient management interface
Fall detection module
Location tracking service
Feedback screen
Help/About sections
Voice command routing framework
Firebase services are connected and functioning correctly. Notification handling and analytics helpers continue to operate as expected. Repeated navigation testing confirmed that fragment transitions do not crash the application.
The mobile repository structure was reviewed and confirmed to be clean and maintainable. Existing Android test files remain intact. All mobile deliverable links in ceng355report.md were tested and confirmed functional.

Group 5 mobile demonstration readiness: confirmed.
## Problems and hyperlinks/urls to potential solutions
Location tracking reliability depends on Android permission handling and background execution limits. Some devices restrict location updates when the app runs in the background.
Impact: The app may temporarily stop updating location data or delay alerts if the operating system limits background services.

Current approach: The group is reviewing Android foreground service and permission best practices to ensure stable location tracking without violating system restrictions. Additional testing will be done across multiple devices and Android versions.

Relevant Android documentation:
Android location services guide
https://developer.android.com/training/location

Foreground services and background limits
https://developer.android.com/guide/components/foreground-services

Runtime permissions handling
https://developer.android.com/training/permissions/requesting
No critical mobile software failures were identified this week. Current issues are related to improving reliability across different Android devices rather than broken functionality.
## Financial
### Expenditures since previous report
No mobile software purchases were required. Development continues using existing Android Studio tools and Firebase free tier.
Cost impact: $0
### Planned future expenses
No mobile-specific expenses expected. Any additional cost will be hardware-related rather than software.
Estimated mobile cost impact: $0
