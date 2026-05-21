# Cardio Data Simulator

The Cardio Data Simulator is a Java-based application designed to simulate real-time cardiovascular data for multiple patients. This tool is particularly useful for educational purposes, enabling students to interact with real-time data streams of ECG, blood pressure, blood saturation, and other cardiovascular signals.

## Features

- Simulate real-time ECG, blood pressure, blood saturation, and blood levels data.
- Supports multiple output strategies:
  - Console output for direct observation.
  - File output for data persistence.
  - WebSocket and TCP output for networked data streaming.
- Configurable patient count and data generation rate.
- Randomized patient ID assignment for simulated data diversity.

## Getting Started

### Prerequisites

- Java JDK 11 or newer.
- Maven for managing dependencies and compiling the application.

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/tpepels/signal_project.git
   ```

2. Navigate to the project directory:

   ```sh
   cd signal_project
   ```

3. Compile and package the application using Maven:
   ```sh
   mvn clean package
   ```
   This step compiles the source code and packages the application into an executable JAR file located in the `target/` directory.

### Running the Simulator

After packaging, you can run the simulator directly from the executable JAR:

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar
```

To run with specific options (e.g., to set the patient count and choose an output strategy):

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --patient-count 100 --output file:./output
```

### Supported Output Options

- `console`: Directly prints the simulated data to the console.
- `file:<directory>`: Saves the simulated data to files within the specified directory.
- `websocket:<port>`: Streams the simulated data to WebSocket clients connected to the specified port.
- `tcp:<port>`: Streams the simulated data to TCP clients connected to the specified port.

## UML Models

The uml_models directory contains four UML class diagrams modeling key subsystems of the Cardiovascular Health Monitoring System (CHMS). Each diagram is provided as a PNG image together with its PlantUML source file.

 Subsystem / Diagram / Description 

 Alert Generation System / [PNG](uml_models/AlertGenerationSystem.png) / Real-time threshold evaluation, alert creation, and dispatch to medical staff 


 Data Storage System / [PNG](uml_models/DataStorageSystem.png) / Secure storage, access control, retention policy, and audit logging 
 Patient Identification System / [PNG](uml_models/PatientIdentificationSystem.png) / Matching incoming data to hospital patient records and handling mismatches 


 Data Access Layer / [PNG](uml_models/DataAccessLayer.png) / Abstracting TCP, WebSocket, and file-based data input into a unified interface 

A detailed written description of each diagram and the rationale behind the design choices is available in [uml_models/descriptions.md](uml_models/descriptions.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Project Members
- Student ID: 6416131