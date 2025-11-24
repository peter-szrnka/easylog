package io.github.easylog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.easylog.model.LogEntry;
import io.github.easylog.model.LogLevel;
import io.github.easylog.model.SaveLogRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void save_shouldCallServiceWithRequestBody() throws Exception {
        // given
        SaveLogRequest request = new SaveLogRequest();
        LogEntry entry = new LogEntry();
        entry.setCorrelationId(UUID.randomUUID().toString());
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
    @ValueSource(strings = {"asc", "desc"})
    void list_shouldReturnPagedResult(String sortDirection) throws Exception {
        // given
        SaveLogRequest request = new SaveLogRequest();
        LogEntry entry = new LogEntry();
        entry.setCorrelationId("1");
        entry.setLogLevel(LogLevel.INFO);
        entry.setTag("test");
        entry.setTimestamp(ZonedDateTime.now());
        entry.setMessage("message");
        entry.setSessionId("session-id");
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
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].message").value("message"))
                .andExpect(jsonPath("$.content[0].correlationId").value("1"));
    }
}