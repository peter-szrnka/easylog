package io.github.easylog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.easylog.controller.LogController;
import io.github.easylog.dao.DefaultLogEntityDao;
import io.github.easylog.dao.LogEntityDao;
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
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @author Peter Szrnka
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EasyLogApplication {

    public static void main() throws IOException {
        int port = Integer.parseInt(getEnv("EASYLOG_SERVER_PORT", "8080"));
        startApp(port);
    }

    public static Javalin startApp(int port) throws IOException {
        String serverDbFile = getEnv("EASYLOG_SERVER_DB_FILE", "easylog.db");
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:" + serverDbFile);
        DefaultWebsocketMessagingClientService websocketMessagingClientService = new DefaultWebsocketMessagingClientService();
        LogEntityDao dao = new DefaultLogEntityDao(jdbi);
        LogService service = new LogService(websocketMessagingClientService, dao);
        LogController controller = new LogController(service);
        JmDnsService jmDnsService = new JmDnsService(
                getEnv("EASYLOG_SERVICE_NAME", "EasyLogService"),
                port,
                getEnv("EASYLOG_SERVICE_NAME", "easylog"),
                Boolean.parseBoolean(getEnv("EASYLOG_SSL_ENABLED", "false"))
        );

        // App start
        var app = Javalin.create(EasyLogApplication::setConfig).start(port);
        app.get("/logs", ctx -> ctx.result(Objects.requireNonNull(Javalin.class.getResourceAsStream("/static/index.html"))).contentType("text/html"));

        controller.registerRoutes(app);
        initWebsocket(app, websocketMessagingClientService);
        app.events(event -> event.serverStopping(jmDnsService::onShutdown));
        jmDnsService.init();

        return app;
    }

    private static void setConfig(JavalinConfig config) {
        boolean isDev = "dev".equals(getEnv("ENV", ""));

        if (isDev) {
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

    private static String getEnv(String key, String defaultValue) {
        return ofNullable(System.getenv(key)).orElse(defaultValue);
    }
}