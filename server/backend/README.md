# EasyLog server application

This folder contains the server-side application for EasyLog, a local logging and monitoring solution. The server application is responsible for receiving, processing, and storing log data sent from various clients.

## Configuration
Before running the server, ensure that you have configured the necessary settings in the `config.properties` file located in the same directory as the server jar file. Key configuration options include:
- `EASYLOG_SERVER_PORT`: The port on which the server will listen for incoming log data (default: 8080).
- `EASYLOG_SERVICE_NAME`: The name of the NSD service for network discovery (default: EasyLogService).
- `EASYLOG_SERVICE_TYPE`: The type of the NSD service (default: _easlog._tcp).

## Firewall Settings
Make sure to allow incoming connections on the port specified in `EASYLOG_SERVER_PORT` through and 5353 UDP for NSD service discovery.

Unsecure alternative: you can disable the regional firewall (if you have permission to do that) temporarily while testing.

## Running the Server

> java -jar easylog-server.jar

## Running with SQLiite database
Configuration:
- `EASYLOG_SERVER_DB_FILE`: Path to the SQLite database file (default: easylog.db).
- `EASYLOG_SERVER_DB_USERNAME` : Username for database access.
- `EASYLOG_SERVER_DB_CREDENTIAL`: Credentials for database access.

> java -jar easylog-server-sqlite.jar --Dspring.profiles.active=file-db
