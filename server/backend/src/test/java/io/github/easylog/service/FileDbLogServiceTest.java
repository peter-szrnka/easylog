package io.github.easylog.service;

import io.github.easylog.data.LogEntity;
import io.github.easylog.data.LogEntityRepository;
import io.github.easylog.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDbLogServiceTest {

    @Mock
    private LogEntityRepository repository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private FileDbLogService fileDbLogService;

    @Test
    void testSave_savesEntriesAndSendsToWebSocket() {
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
        fileDbLogService.save(request);

        // then
        verify(repository, times(2)).save(any(LogEntity.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), eq(request));
    }

    @Test
    void testList_returnsMappedLogEntries() {
        // given
        LogEntity entity = LogEntity.builder()
                .level(LogLevel.INFO)
                .message("Some message")
                .timestamp(ZonedDateTime.now())
                .tag("TAG")
                .sessionId("SESSION1")
                .messageId("CORR1")
                .build();

        Page<LogEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAll(Mockito.<Specification<LogEntity>>any(), any(Pageable.class))).thenReturn(entityPage);

        SearchRequest searchRequest = SearchRequest.builder()
                .pageRequest(PageRequest.builder().size(10).sortBy("timestamp").build())
                .from(ZonedDateTime.now().minusDays(1L))
                .build();

        // when
        PageResponse<LogEntry> result = fileDbLogService.list(searchRequest);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        LogEntry logEntry = result.getContent().getFirst();
        assertEquals(entity.getLevel(), logEntry.getLogLevel());
        assertEquals(entity.getMessage(), logEntry.getMessage());
        assertEquals(entity.getTimestamp(), logEntry.getTimestamp());
        assertEquals(entity.getTag(), logEntry.getTag());
        assertEquals(entity.getSessionId(), logEntry.getSessionId());
        assertEquals(entity.getMessageId(), logEntry.getMessageId());
    }
}