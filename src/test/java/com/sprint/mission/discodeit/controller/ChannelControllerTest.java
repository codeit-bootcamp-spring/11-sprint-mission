package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ChannelService channelService;

    private ChannelDto publicChannelDto(String name) {
        return new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, name, null, List.of(), null);
    }

    @Test
    void createPublic_정상_201반환() throws Exception {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest("general", "설명");
        given(channelService.create(any(PublicChannelCreateRequest.class)))
            .willReturn(publicChannelDto("general"));

        mockMvc.perform(post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("general"));
    }

    @Test
    void createPublic_유효성실패_400반환() throws Exception {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest("", null);

        mockMvc.perform(post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createPrivate_정상_201반환() throws Exception {
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
            List.of(UUID.randomUUID(), UUID.randomUUID())
        );
        ChannelDto channelDto = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE, null, null, List.of(), null);
        given(channelService.create(any(PrivateChannelCreateRequest.class))).willReturn(channelDto);

        mockMvc.perform(post("/api/channels/private")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.type").value("PRIVATE"));
    }

    @Test
    void update_정상_200반환() throws Exception {
        UUID channelId = UUID.randomUUID();
        PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newname", "새설명");
        given(channelService.update(eq(channelId), any())).willReturn(publicChannelDto("newname"));

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("newname"));
    }

    @Test
    void delete_정상_204반환() throws Exception {
        UUID channelId = UUID.randomUUID();
        willDoNothing().given(channelService).delete(channelId);

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_없는채널_404반환() throws Exception {
        UUID channelId = UUID.randomUUID();
        willThrow(new ChannelNotFoundException()).given(channelService).delete(channelId);

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
    }

    @Test
    void findAll_정상_200반환() throws Exception {
        UUID userId = UUID.randomUUID();
        given(channelService.findAllByUserId(userId))
            .willReturn(List.of(publicChannelDto("ch1"), publicChannelDto("ch2")));

        mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
}