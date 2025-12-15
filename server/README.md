# EasyLog server application
**UNDER DEVELOPMENT!!!**


This folder contains the server-side application for EasyLog, a local logging and monitoring library solution (https://github.com/peter-szrnka/easylog-android-client-library).
The server application is responsible for receiving, processing, and storing log data sent from various clients.

**The database provider is SQLite, because this whole library and server designed to test your application locally.**

You can use 2 types of servers:

- Javalin: For simple application tests,
- Spring Boot: Useful for larger workloads

## Configuration
### Create a P12 keystore

> keytool -genkeypair -alias easylog -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore easylog-keystore.p12 -validity 365 -storepass easylog123 -keypass easylog123 -dname "CN=localhost, OU=Development, O=EasyLog, L=City, ST=State, C=US"

### Javalin

You need to define a configuration file with the following attributes:

- `SERVER_PORT`: The port on which the server will listen for incoming log data (default: 8080).
- `SERVICE_NAME`: The name of the NSD service for network discovery (default: EasyLogService).
- `SERVICE_TYPE`: The type of the NSD service (default: _easyLog._tcp).
- `SERVER_DB_FILE`: Path to the SQLite database file (default: easylog.db).
- `SSL_ENABLED`: (Optional) You can disable HTTPS, but in that case you need to handle the possible failures in your Android application.
- `SSL_KEYSTORE`: Keystore file's path, e.g.: C:/dev/projects/easylog-keystore.p12
- `SSL_KEYSTORE_PASSWORD`: Password for the keystore

**To start the app, run:**

> java -jar easylog-desktop-<version>.jar application.properties

### Spring Boot

Before running the server, ensure that you have configured the necessary settings in the `config.properties` file located in the same directory as the server jar file. Key configuration options include:

- `SERVER_PORT`: The port on which the server will listen for incoming log data (default: 8080).
- `SERVICE_NAME`: The name of the NSD service for network discovery (default: EasyLogService).
- `SERVICE_TYPE`: The type of the NSD service (default: _easyLog._tcp).
- `SERVER_DB_FILE`: Path to the SQLite database file (default: easylog.db).

**To start the app, run:**

> java -jar easylog-desktop-<version>.jar

## Firewall Settings
Make sure to allow incoming connections on the port specified in `EASYLOG_SERVER_PORT` through and 5353 UDP for NSD service discovery.

**Unsecure alternative: you can disable the regional firewall (if you have permission to do that) temporarily while testing.**
