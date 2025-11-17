package com.irctc.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irctc.booking.dto.offline.*;
import com.irctc.booking.entity.OfflineAction;
import com.irctc.booking.service.OfflineActionService;
import com.irctc.booking.service.OfflineSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OfflineSyncControllerTest {

    @Mock
    private OfflineSyncService offlineSyncService;

    @Mock
    private OfflineActionService offlineActionService;

    @InjectMocks
    private OfflineSyncController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Test
    void shouldSyncOfflineBundle() throws Exception {
        OfflineSyncResponse response = new OfflineSyncResponse();
        OfflineSyncResponse.SyncMetadata metadata = new OfflineSyncResponse.SyncMetadata();
        metadata.setTicketCount(2);
        response.setMetadata(metadata);
        when(offlineSyncService.generateOfflineBundle(any(OfflineSyncRequest.class))).thenReturn(response);

        OfflineSyncRequest request = new OfflineSyncRequest();
        request.setUserId(1L);

        mockMvc.perform(post("/api/offline/sync")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metadata.ticketCount").value(2));
    }

    @Test
    void shouldQueueOfflineAction() throws Exception {
        OfflineAction action = new OfflineAction();
        action.setId(10L);
        action.setUserId(1L);
        action.setActionType("CHECK_IN");
        action.setStatus("QUEUED");
        action.setQueuedAt(LocalDateTime.now());

        when(offlineActionService.queueAction(any(OfflineActionRequest.class))).thenReturn(action);

        OfflineActionRequest request = new OfflineActionRequest();
        request.setUserId(1L);
        request.setActionType("CHECK_IN");

        mockMvc.perform(post("/api/offline/actions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void shouldReturnPendingActions() throws Exception {
        OfflineAction action = new OfflineAction();
        action.setId(11L);
        action.setUserId(1L);
        action.setStatus("QUEUED");

        when(offlineActionService.getPendingActions(eq(1L))).thenReturn(List.of(action));

        mockMvc.perform(get("/api/offline/actions/pending").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(11L));
    }
}

