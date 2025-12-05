package io.github.easylog.service;

import io.github.easylog.dao.LogEntityDao;
import io.github.easylog.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogEntityDao dao;

    @Mock
    private WebsocketMessagingClientService websocketMessagingClientService;

    @InjectMocks
    private LogService service;

    @Test
    void testSave_savesEntriesAndSendsToWebSocket() throws Exception {
        // given
        LogEntry entry1 = new LogEntry();
        entry1.setLogLevel(LogLevel.INFO);
        entry1.setMessage("Test message 1");
        entry1.setTimestamp(ZonedDateTime.now());

        LogEntry entry2 = new LogEntry();
        entry2.setLogLevel(LogLevel.ERROR);
        entry2.setMessage("Test message 2");
        entry2.setTimestamp(ZonedDateTime.now());

        SaveLogRequest request = new SaveLogRequest();
        request.setEntries(List.of(entry1, entry2));

        // when
        service.save(request);

        // then
        verify(dao).save(any(SaveLogRequest.class));
        verify(websocketMessagingClientService, times(1)).convertAndSend(anyString(), eq(request));
    }

    @Test
    void testList_returnsEmptyList() {
        // given
        SearchRequest searchRequest = SearchRequest.builder()
                .pageRequest(PageRequest.builder().size(10).sortBy("timestamp").build())
                .from(ZonedDateTime.now().minusDays(1L))
                .dateRangeType(DateRangeType.LIVE)
                .build();

        // when
        PageResponse<LogEntry> result = service.list(searchRequest);

        // then
        assertNotNull(result);
    }

    @Test
    void testList_returnsMappedLogEntries() {
        // given
        LogEntry entity = new LogEntry();
        entity.setLogLevel(LogLevel.INFO);
        entity.setMessage("Some message");
        entity.setTimestamp(ZonedDateTime.now());
        entity.setTag("TAG");
        entity.setSessionId("SESSION1");
        entity.setMessageId("CORR1");

        PageResponse<LogEntry> entityPage = PageResponse.<LogEntry>builder().content(List.of(entity)).build();
        when(dao.findAll(Mockito.any(SearchRequest.class))).thenReturn(entityPage);

        SearchRequest searchRequest = SearchRequest.builder()
                .pageRequest(PageRequest.builder().size(10).sortBy("timestamp").build())
                .from(ZonedDateTime.now().minusDays(1L))
                .dateRangeType(DateRangeType.LAST_1_MONTH)
                .build();

        // when
        PageResponse<LogEntry> result = service.list(searchRequest);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        LogEntry logEntry = result.getContent().getFirst();
        assertEquals(entity.getLogLevel(), logEntry.getLogLevel());
        assertEquals(entity.getMessage(), logEntry.getMessage());
        assertEquals(entity.getTimestamp(), logEntry.getTimestamp());
        assertEquals(entity.getTag(), logEntry.getTag());
        assertEquals(entity.getSessionId(), logEntry.getSessionId());
        assertEquals(entity.getMessageId(), logEntry.getMessageId());
    }

    @Test
    void convertAndSend_shouldWrapExceptionInRuntimeException() throws Exception {
        // given
        LogEntry entry1 = new LogEntry();
        entry1.setLogLevel(LogLevel.INFO);
        entry1.setMessage("Test message 1");
        entry1.setTimestamp(ZonedDateTime.now());

        SaveLogRequest request = new SaveLogRequest();
        request.setEntries(List.of(entry1));
        doThrow(new IllegalArgumentException("Oops")).when(websocketMessagingClientService).convertAndSend("/topic/logs", request);

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.save(request));

        // then
        assertThat(exception.getMessage()).isEqualTo("java.lang.IllegalArgumentException: Oops");
    }
}