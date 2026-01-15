**FLIGHT SIMULATOR (Java | jMonkeyEngine)**

A simple 3D flight simulation project developed during my 2nd year of engineering as a learning exercise. The focus
of this project is on 3D rendering, camera systems, and controls rather than realistic aerodynamics or physics.
Contributions and improvements are welcome.


**OVERVIEW**

This project implements a basic 3D flight simulation environment featuring: Aircraft movement with simplified
controls 3D terrain rendering Dynamic third-person camera system Keyboard and mouse-based control scheme
Efficient real-time rendering using jMonkeyEngine Note: Realistic flight physics are not implemented. This project is
intended as a learning and experimentation platform.


**TECHNICAL SPECIFICATIONS**

System Requirements
Operating System: Windows / Linux / macOS
Java: JDK 17 or later
Memory: 4 GB RAM minimum (8 GB recommended)
Graphics: OpenGL 3.3+ compatible GPU
Storage: Approximately 500 MB free space
Dependencies
jMonkeyEngine 3.5.2 – 3D game engine
LWJGL – OpenGL and input handling
OpenAL – 3D audio support


**GETTING STARTED**

Prerequisites
1. Install JDK 17 or later
2. Configure Java environment variables
3. Install Apache Ant


Running the Application
java -jar dist/FLIGHT_SIMULATOR.jar


**CONTROLS**

Arrow Keys : Aircraft movement
W / S : Altitude control
A / D : Yaw control
Mouse : Camera control
ESC : Exit simulation


**BUILDING FROM SOURCE**

1. Ensure all prerequisites are installed
2. Clone the repository
3. Navigate to the project root
4. Run ant
5. The compiled JAR will be generated in the dist directory


**CONTRIBUTING**

Contributions are welcome and appreciated. Suggested improvement areas include: Basic flight physics Terrain
optimization HUD or cockpit UI Improved camera modes Performance enhancements


ACKNOWLEDGMENTS
jMonkeyEngine Team for the 3D engine
LWJGL for high-performance graphics and input
OpenAL for spatial audio support
