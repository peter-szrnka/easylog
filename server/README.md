# EasyLog server application
**UNDER DEVELOPMENT!!!**

This folder contains the server-side application for **EasyLog** local logging and monitoring library solution (https://github.com/peter-szrnka/easylog).
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

**Create easylog-desktop.properties config file**

Example:

```
SERVER_PORT=8080
SERVER_DB_FILE=C:/dev/projects/easylog-test.db
SSL_KEYSTORE=C:/dev/projects/easylog-keystore.p12
SSL_KEYSTORE_PASSWORD=easylog123
```



**To start the app, run:**

> java -jar easylog-desktop-<version>.jar easylog-desktop.properties

### Spring Boot

You have 2 options to configure the app here:

- Provide the mandatory environment variables, or
- Define an application.properties file in the same folder where you want to start the JAR file.

**Option 1 (recommended):**

| Environment Variable            | Default Value                    | Description                                              |
| ------------------------------- | -------------------------------- | -------------------------------------------------------- |
| `EASYLOG_SERVER_PORT`           | `8080`                           | The port on which the EasyLog server will run.           |
| `EASYLOG_SSL_ENABLED`           | `true`                           | Enables or disables SSL for the server.                  |
| `EASYLOG_SSL_KEYSTORE`          | `classpath:easylog-keystore.p12` | Path to the SSL keystore file.                           |
| `EASYLOG_SSL_KEYSTORE_PASSWORD` | `easylog123`                     | Password for the SSL keystore.                           |
| `EASYLOG_SERVER_DB_FILE`        | `./data/easylog-server.db`       | Path to the SQLite database file.                        |
| `EASYLOG_SERVER_DB_USERNAME`    | `sa`                             | Username for database connection.                        |
| `EASYLOG_SERVER_DB_CREDENTIAL`  | *(none)*                         | Password for the database connection (must be provided). |
| `EASYLOG_SERVICE_NAME`          | `EasyLogService`                 | The display name of the EasyLog service.                 |
| `EASYLOG_SERVICE_TYPE`          | `easylog`                        | Type or identifier of the EasyLog service.               |

**Option 2:**

Create the application.properties file:

```
spring.application.name=easylog-server
server.port=8443

server.ssl.enabled=true
server.ssl.key-store=classpath:easylog-keystore.p12
server.ssl.key-store-password=easylog123
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=easylog

# Optional: Enable HTTP/2
server.http2.enabled=true

spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.url=jdbc:sqlite:easylog-server.db
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.datasource.hikari.max-lifetime=600000
spring.datasource.hikari.maximum-pool-size=1

spring.jpa.hibernate.ddl-auto=update

config.easylog.service.name=EasyLogService
config.easylog.service.type=easyLog
```



The downside of this option is that you need to define all properties, even Spring Boot based ones as well.

**To start the app, run:**

> java -jar easylog-desktop-<version>.jar

## Firewall Settings
Make sure to allow incoming connections on the port specified in `EASYLOG_SERVER_PORT` through and 5353 UDP for NSD service discovery.

**Unsecure alternative: you can disable the regional firewall (if you have permission to do that) temporarily while testing.**
