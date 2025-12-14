package io.github.easylog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.easylog.controller.LogController;
import io.github.easylog.dao.DefaultLogEntityDao;
import io.github.easylog.dao.LogEntityDao;
import io.github.easylog.model.EnvironmentVariable;
import io.github.easylog.model.ServerConfig;
import io.github.easylog.service.DefaultWebsocketMessagingClientService;
import io.github.easylog.service.JmDnsService;
import io.github.easylog.service.LogService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import io.javalin.plugin.bundled.CorsPluginConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.Objects;

import static io.github.easylog.model.EnvironmentVariable.*;
import static java.util.Optional.ofNullable;

/**
 * @author Peter Szrnka
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EasyLogApplication {

    public static void main() throws IOException {
        startApp(new ServerConfig(
                Integer.parseInt(getEnv(SERVER_PORT)),
                Boolean.parseBoolean(getEnv(SSL_ENABLED)),
                getEnv(SERVICE_NAME),
                getEnv(SERVICE_TYPE),
                getEnv(SERVER_DB_FILE),
                getEnv(ENV),
                getEnv(SSL_KEYSTORE),
                getEnv(SSL_KEYSTORE_PASSWORD)
        ));
    }

    public static Javalin startApp(ServerConfig serverConfig) throws IOException {
        DefaultWebsocketMessagingClientService websocketMessagingClientService = new DefaultWebsocketMessagingClientService();
        LogEntityDao dao = createDao(serverConfig);
        LogService service = new LogService(websocketMessagingClientService, dao);
        LogController controller = new LogController(service);
        JmDnsService jmDnsService = createJmDnsService(serverConfig);

        // App start
        var app = Javalin.create(config -> setConfig(config, serverConfig)).start(serverConfig.port());
        app.get("/logs", ctx -> ctx.result(Objects.requireNonNull(Javalin.class.getResourceAsStream("/static/index.html"))).contentType("text/html"));

        controller.registerRoutes(app);
        initWebsocket(app, websocketMessagingClientService);
        app.events(event -> event.serverStopping(jmDnsService::onShutdown));
        jmDnsService.init();

        return app;
    }

    private static JmDnsService createJmDnsService(ServerConfig serverConfig) {
        return new JmDnsService(
                serverConfig.serviceName(),
                serverConfig.port(),
                serverConfig.serviceType(),
                serverConfig.sslEnabled()
        );
    }

    private static LogEntityDao createDao(ServerConfig serverConfig) {
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:" + serverConfig.serverDbFile());
        return new DefaultLogEntityDao(jdbi);
    }

    private static void setConfig(JavalinConfig config, ServerConfig serverConfig) {
        configureSsl(config, serverConfig);

        if ("dev".equals(serverConfig.env())) {
            log.info("dev profile is active");
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            log.info("Custom CORS initialized");
        }

        config.useVirtualThreads = true;
        config.staticFiles.add(staticFiles -> {
            staticFiles.directory = "static";
            staticFiles.hostedPath = "/";
        });
        config.http.defaultContentType = "application/json";

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        config.jsonMapper(new JavalinJackson(objectMapper, true));
    }

    private static void initWebsocket(Javalin app, DefaultWebsocketMessagingClientService websocketMessagingClientService) {
        app.ws("/topic/logs", ws -> {
            ws.onConnect(websocketMessagingClientService::register);
            ws.onClose(websocketMessagingClientService::unregister);
        });
        log.info("Websocket initialized");
    }

    private static void configureSsl(JavalinConfig config, ServerConfig serverConfig) {
        if (!serverConfig.sslEnabled()) {
            return;
        }

        config.jetty.modifyServer(server -> {
            SslContextFactory.Server sslContextFactory = getServer(serverConfig);

            HttpConfiguration httpsConfig = new HttpConfiguration();
            httpsConfig.setSecureScheme("https");
            httpsConfig.setSecurePort(serverConfig.port());
            httpsConfig.addCustomizer(new SecureRequestCustomizer());

            ServerConnector sslConnector = new ServerConnector(
                    server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(httpsConfig)
            );
            sslConnector.setPort(serverConfig.port());

            server.addConnector(sslConnector);
            log.info("HTTPS enabled on port {}", serverConfig.port());
        });
    }

    private static SslContextFactory.Server getServer(ServerConfig serverConfig) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        String keystorePath = serverConfig.sslKeystore();
        if (keystorePath == null) {
            throw new IllegalArgumentException("Environment variable SSL_KEYSTORE is mandatory!");
        }

        String keystorePassword = serverConfig.sslKeystorePassword();
        if (keystorePassword == null) {
            throw new IllegalArgumentException("Environment variable SSL_KEYSTORE_PASSWORD is mandatory!");
        }

        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(keystorePassword);
        sslContextFactory.setKeyStoreType("PKCS12");
        return sslContextFactory;
    }

    private static String getEnv(EnvironmentVariable variable) {
        return ofNullable(System.getenv(variable.getName())).orElse(variable.getDefaultValue());
    }
}