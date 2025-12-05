package io.github.easylog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.easylog.model.DateRangeType;
import io.github.easylog.model.LogEntry;
import io.github.easylog.model.LogLevel;
import io.github.easylog.model.SaveLogRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.github.easylog.model.DateRangeType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Peter Szrnka
 */
@SpringBootTest
@AutoConfigureMockMvc
class LogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Test
    void save_shouldCallServiceWithRequestBody() throws Exception {
        // given
        SaveLogRequest request = new SaveLogRequest();
        LogEntry entry = new LogEntry();
        entry.setMessageId(UUID.randomUUID().toString());
        entry.setLogLevel(LogLevel.INFO);
        entry.setTag("test");
        entry.setTimestamp(ZonedDateTime.now());
        entry.setMessage("message");
        entry.setSessionId("session-id");
        request.setEntries(List.of(entry));

        // when
        mockMvc.perform(post("/api/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<SaveLogRequest> captor = ArgumentCaptor.forClass(SaveLogRequest.class);
    }

    @ParameterizedTest
    @MethodSource("listInputData")
    void list_shouldReturnPagedResult(String filter, String sortDirection, String startDate, String endDate, DateRangeType dateRangeType, Map<String, String> metadata) throws Exception {
        // given
        SaveLogRequest request = new SaveLogRequest();
        LogEntry entry = new LogEntry();
        entry.setMessageId("1");
        entry.setLogLevel(LogLevel.INFO);
        entry.setTag("test");
        entry.setTimestamp(ZonedDateTime.now());
        entry.setMessage(filter);
        entry.setSessionId("session-id");
        entry.setMetadata(metadata);
        request.setEntries(List.of(entry));

        mockMvc.perform(post("/api/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());


        // when & then
        mockMvc.perform(get("/api/log")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "timestamp")
                        .param("sortDirection", sortDirection)
                        .param("filter", filter)
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param("dateRangeType", dateRangeType.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].messageId").value("1"));
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

                Arguments.of("message", "asc", formatter.format(ZonedDateTime.now().minusDays(1)), null, CUSTOM, null),
                Arguments.of("message", "asc", null, formatter.format(ZonedDateTime.now().plusDays(1)), CUSTOM, null),
                Arguments.of("message", "asc", formatter.format(ZonedDateTime.now().minusDays(1)), formatter.format(ZonedDateTime.now().plusDays(1)), CUSTOM, null)
        );
    }
}