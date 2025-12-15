package io.github.easylog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.easylog.model.*;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.github.easylog.converter.Converters.FORMATTER;
import static io.github.easylog.model.DateRangeType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Peter Szrnka
 */
class SecureServerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static Javalin app;
    private static final int PORT = 8081;

    @BeforeAll
    static void startServer() throws IOException {
        Files.deleteIfExists(Paths.get("easylog-test.db"));

        app = EasyLogApplication.startApp(new ServerConfig(
                PORT,
                true,
                "EasyLogService",
                "easyLog",
                "easylog-test.db",
                "",
                "src/test/resources/easylog-keystore.p12",
                "easylog123"
        ));
    }

    @AfterAll
    static void stopServer() throws IOException {
        if (app != null) {
            app.stop();
        }

        Files.deleteIfExists(Paths.get("easylog-test.db"));
    }

    @ParameterizedTest
    @MethodSource("inputData")
    void whenWrongInputDataProvided_thenThrowException(String sslKeystore, String sslKeystorePassword) {
        // given
        ServerConfig serverConfig = new ServerConfig(
                PORT,
                true,
                "EasyLogService",
                "easyLog",
                "easylog-test.db",
                "",
                sslKeystore,
                sslKeystorePassword
        );

        // when & then
        assertThrows(IllegalArgumentException.class, () -> EasyLogApplication.startApp(serverConfig));
    }


    @ParameterizedTest
    @MethodSource("saveInputData")
    void save_shouldCallServiceWithRequestBody(ZonedDateTime input) throws Exception {
        // given
        disableSslVerification();
        SaveLogRequest request = new SaveLogRequest();
        LogEntry entry = new LogEntry();
        entry.setLogEntryId(UUID.randomUUID().toString());
        entry.setLogLevel(LogLevel.INFO);
        entry.setTag("test");
        entry.setTimestamp(input);
        entry.setMessage("message");
        entry.setSessionId("session-id");
        request.setEntries(List.of(entry));

        // when
        HttpURLConnection connection = postJson(objectMapper.writeValueAsString(request));

        // then
        assertEquals(201, connection.getResponseCode());
    }

    @ParameterizedTest
    @MethodSource("listInputData")
    void list_shouldReturnPagedResult(String filter, String sortDirection, String startDate, String endDate, DateRangeType dateRangeType, Map<String, String> metadata) throws Exception {
        // given
        disableSslVerification();
        SaveLogRequest request = new SaveLogRequest();
        LogEntry entry = new LogEntry();
        entry.setLogEntryId(UUID.randomUUID().toString());
        entry.setLogLevel(LogLevel.INFO);
        entry.setTag("test");
        entry.setTimestamp(ZonedDateTime.now());
        entry.setMessage("message");
        entry.setSessionId(UUID.randomUUID().toString());
        entry.setMetadata(metadata);
        request.setEntries(List.of(entry));

        HttpURLConnection postConnection = postJson(objectMapper.writeValueAsString(request));
        assertEquals(201, postConnection.getResponseCode());

        StringBuilder stringBuilder = new StringBuilder("/api/log?");

        if(filter != null) {
            stringBuilder.append("filter=").append(filter).append("&");
        }

        stringBuilder.append("sortDirection=").append(sortDirection).append("&");
        stringBuilder.append("sortBy=timestamp&page=0&size=10&");
        stringBuilder.append("dateRangeType=").append(dateRangeType.name()).append("&");

        if(startDate != null) {
            stringBuilder.append("startDate=").append(startDate).append("&");
        }

        if(endDate != null) {
            stringBuilder.append("endDate=").append(endDate);
        }

        // when
        HttpURLConnection getConnection = get(stringBuilder.toString());

        // then
        assertEquals(200, getConnection.getResponseCode());
    }

    private static Stream<Arguments> inputData() {
        return Stream.of(
                Arguments.of(null, ""),
                Arguments.of("", null)
        );
    }

    private static HttpURLConnection postJson(String json) throws IOException {
        URL url = URI.create("https://localhost:" + PORT + "/api/log").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        return connection;
    }

    private static HttpURLConnection get(String path) throws IOException {
        URL url = URI.create("https://localhost:" + PORT + path).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    private static Stream<Arguments> saveInputData() {
        return Stream.of(
                Arguments.of((ZonedDateTime) null),
                Arguments.of(ZonedDateTime.now())
        );
    }

    private static Stream<Arguments> listInputData() {
        return Stream.of(
                Arguments.of("message", "asc", null, null, LAST_1_DAY, null),
                Arguments.of("message", "desc", null, null, LAST_1_DAY, Map.of()),

                Arguments.of("message", "desc", null, null, LAST_1_DAY, Map.of("username", "test", "deviceId", "12345678")),
                Arguments.of("message", "desc", null, null, LAST_1_HOUR, null),
                Arguments.of(null, "desc", null, null, LAST_1_MONTH, null),
                Arguments.of("", "desc", null, null, LAST_1_MONTH, null),
                Arguments.of("message", "desc", null, null, LAST_4_HOURS, null),
                Arguments.of("message", "desc", null, null, LAST_5_MINUTES, null),
                Arguments.of("message", "desc", null, null, LAST_7_DAYS, null),
                Arguments.of("message", "desc", null, null, LAST_15_MINUTES, null),
                Arguments.of("message", "desc", null, null, LAST_30_MINUTES, null),

                Arguments.of("message", "asc", FORMATTER.format(ZonedDateTime.now().minusDays(1)), null, CUSTOM, null),
                Arguments.of("message", "asc", null, FORMATTER.format(ZonedDateTime.now().plusDays(1)), CUSTOM, null),
                Arguments.of("message", "asc", FORMATTER.format(ZonedDateTime.now().minusDays(1)), FORMATTER.format(ZonedDateTime.now().plusDays(1)), CUSTOM, null)
        );
    }

    private static void disableSslVerification() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Certs must be accepted automatically
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Certs must be accepted automatically
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}