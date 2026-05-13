package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReadStatusController.class)
class ReadStatusControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ReadStatusService readStatusService;

    private ReadStatusDto readStatusDto(UUID userId, UUID channelId) {
        return new ReadStatusDto(UUID.randomUUID(), userId, channelId, Instant.now());
    }

    @Test
    void create_정상_201반환() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());
        given(readStatusService.create(any())).willReturn(readStatusDto(userId, channelId));

        mockMvc.perform(post("/api/readStatuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.channelId").value(channelId.toString()));
    }

    @Test
    void update_정상_200반환() throws Exception {
        UUID readStatusId = UUID.randomUUID();
        ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(Instant.now());
        ReadStatusDto dto = readStatusDto(UUID.randomUUID(), UUID.randomUUID());
        given(readStatusService.update(eq(readStatusId), any())).willReturn(dto);

        mockMvc.perform(patch("/api/readStatuses/{readStatusId}", readStatusId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dto.id().toString()));
    }

    @Test
    void update_없는ReadStatus_404반환() throws Exception {
        UUID readStatusId = UUID.randomUUID();
        ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(Instant.now());
        willThrow(new ReadStatusNotFoundException()).given(readStatusService).update(eq(readStatusId), any());

        mockMvc.perform(patch("/api/readStatuses/{readStatusId}", readStatusId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("READ_STATUS_NOT_FOUND"));
    }

    @Test
    void findAllByUserId_정상_200반환() throws Exception {
        UUID userId = UUID.randomUUID();
        given(readStatusService.findAllByUserId(userId)).willReturn(
            List.of(readStatusDto(userId, UUID.randomUUID()), readStatusDto(userId, UUID.randomUUID()))
        );

        mockMvc.perform(get("/api/readStatuses").param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
}