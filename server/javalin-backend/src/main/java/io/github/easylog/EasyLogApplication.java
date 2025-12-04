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
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;

@Slf4j
public class EasyLogApplication {

    public static void main() throws IOException {
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:easylog.db");
        DefaultWebsocketMessagingClientService websocketMessagingClientService = new DefaultWebsocketMessagingClientService();
        LogEntityDao dao = new DefaultLogEntityDao(jdbi);
        LogService service = new LogService(websocketMessagingClientService, dao);
        LogController controller = new LogController(service);
        JmDnsService jmDnsService = new JmDnsService("EasyLogService", 8080, "easylog", false);

        // App start
        var app = Javalin.create(EasyLogApplication::setConfig).start(8080);

        controller.registerRoutes(app);
        initWebsocket(app, websocketMessagingClientService);
        app.events(event -> event.serverStopping(jmDnsService::onShutdown));
        jmDnsService.init();
    }

    private static void setConfig(JavalinConfig config) {
        boolean isDev = "dev".equals(System.getenv("env"));

        if (isDev) {
            log.info("dev profile is active");
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            log.info("Custom CORS initialized");
        }

        config.useVirtualThreads = true;
        config.staticFiles.add("static");
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
}