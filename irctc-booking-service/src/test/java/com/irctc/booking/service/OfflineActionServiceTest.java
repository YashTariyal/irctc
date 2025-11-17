package com.irctc.booking.service;

import com.irctc.booking.dto.offline.OfflineActionRequest;
import com.irctc.booking.entity.OfflineAction;
import com.irctc.booking.repository.OfflineActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OfflineActionServiceTest {

    @Mock
    private OfflineActionRepository repository;

    @InjectMocks
    private OfflineActionService offlineActionService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldQueueAction() {
        OfflineActionRequest request = new OfflineActionRequest();
        request.setUserId(1L);
        request.setBookingId(2L);
        request.setActionType("CHECK_IN");
        request.setPayload("{\"pnr\":\"PNR\"}");

        OfflineAction saved = new OfflineAction();
        saved.setId(10L);
        saved.setUserId(1L);
        saved.setActionType("CHECK_IN");

        when(repository.save(any())).thenReturn(saved);

        OfflineAction result = offlineActionService.queueAction(request);

        assertThat(result.getId()).isEqualTo(10L);
        ArgumentCaptor<OfflineAction> captor = ArgumentCaptor.forClass(OfflineAction.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("QUEUED");
    }

    @Test
    void shouldProcessPendingActions() {
        OfflineAction action = new OfflineAction();
        action.setId(1L);
        action.setStatus("QUEUED");
        action.setUserId(1L);

        when(repository.findByUserIdAndStatusIn(eq(1L), any(Set.class))).thenReturn(List.of(action));
        when(repository.save(any(OfflineAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<OfflineAction> processed = offlineActionService.processPendingActions(1L);

        assertThat(processed).hasSize(1);
        assertThat(processed.get(0).getStatus()).isEqualTo("COMPLETED");
        verify(repository, atLeast(2)).save(any(OfflineAction.class));
    }
}

