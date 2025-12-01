package io.github.easylog.service;

import io.github.easylog.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleLogServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private SimpleLogService simpleLogService;

    @Test
    void testSave_addsLogsAndSendsToWebSocket() {
        // given
        LogEntry entry1 = new LogEntry();
        entry1.setLogLevel(LogLevel.INFO);
        entry1.setMessage("Message 1");
        entry1.setTimestamp(ZonedDateTime.now());

        LogEntry entry2 = new LogEntry();
        entry2.setLogLevel(LogLevel.ERROR);
        entry2.setMessage("Message 2");
        entry2.setTimestamp(ZonedDateTime.now());

        SaveLogRequest request = new SaveLogRequest();
        request.setEntries(List.of(entry1, entry2));

        // when
        simpleLogService.save(request);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), eq(request));

        PageResponse<LogEntry> result = simpleLogService.list(SearchRequest.builder().pageRequest(io.github.easylog.model.PageRequest.builder().page(0).size(5).build()).build());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().containsAll(List.of(entry1, entry2)));
    }

    @Test
    void testList_returnsPaginatedLogs() {
        // given
        LogEntry entry = new LogEntry();
        entry.setLogLevel(LogLevel.INFO);
        entry.setMessage("Test message");
        entry.setTimestamp(ZonedDateTime.now());

        SaveLogRequest request = new SaveLogRequest();
        request.setEntries(List.of(entry));
        simpleLogService.save(request);

        SearchRequest searchRequest = SearchRequest.builder().pageRequest(io.github.easylog.model.PageRequest.builder().page(0).size(5).build()).build();

        // when
        PageResponse<LogEntry> result = simpleLogService.list(searchRequest);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(entry, result.getContent().getFirst());
        assertEquals(1, result.getTotalElements());
    }
}
