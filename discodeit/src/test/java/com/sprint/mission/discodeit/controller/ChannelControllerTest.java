package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.service.dto.channel.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.CreatePublicChannelRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ChannelService channelService;

    @Test
    void createPublicChannel_성공_201() throws Exception {
        CreatePublicChannelRequest request = new CreatePublicChannelRequest("일반", "일반 채널");
        ChannelDto response = ChannelDto.builder()
                .id(UUID.randomUUID())
                .type(ChannelType.PUBLIC)
                .name("일반")
                .description("일반 채널")
                .participants(List.of())
                .build();

        given(channelService.createPublicChannel(any())).willReturn(response);

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("일반"))
                .andExpect(jsonPath("$.type").value("PUBLIC"));
    }

    @Test
    void createPublicChannel_이름_없음_400() throws Exception {
        CreatePublicChannelRequest request = new CreatePublicChannelRequest("", "설명");

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    void createPrivateChannel_성공_201() throws Exception {
        UUID participantId = UUID.randomUUID();
        CreatePrivateChannelRequest request = new CreatePrivateChannelRequest(List.of(participantId));
        ChannelDto response = ChannelDto.builder()
                .id(UUID.randomUUID())
                .type(ChannelType.PRIVATE)
                .participants(List.of())
                .build();

        given(channelService.createPrivateChannel(any())).willReturn(response);

        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PRIVATE"));
    }

    @Test
    void createPrivateChannel_참여자_없음_400() throws Exception {
        CreatePrivateChannelRequest request = new CreatePrivateChannelRequest(List.of());

        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void updateChannel_성공_200() throws Exception {
        UUID channelId = UUID.randomUUID();
        ChannelDto response = ChannelDto.builder()
                .id(channelId)
                .type(ChannelType.PUBLIC)
                .name("수정된채널")
                .build();

        given(channelService.update(any())).willReturn(response);

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newName\":\"수정된채널\",\"newDescription\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된채널"));
    }

    @Test
    void updateChannel_비공개채널_400() throws Exception {
        UUID channelId = UUID.randomUUID();
        willThrow(new PrivateChannelUpdateException(channelId)).given(channelService).update(any());

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newName\":\"수정시도\",\"newDescription\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED"));
    }

    @Test
    void deleteChannel_성공_204() throws Exception {
        UUID channelId = UUID.randomUUID();
        willDoNothing().given(channelService).delete(channelId);

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteChannel_채널_없음_404() throws Exception {
        UUID channelId = UUID.randomUUID();
        willThrow(new ChannelNotFoundException(channelId)).given(channelService).delete(channelId);

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
    }

    @Test
    void findAllByUserId_성공_200() throws Exception {
        UUID userId = UUID.randomUUID();
        ChannelDto channel = ChannelDto.builder()
                .id(UUID.randomUUID())
                .type(ChannelType.PUBLIC)
                .name("일반")
                .build();

        given(channelService.findAllByUserId(userId)).willReturn(List.of(channel));

        mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("일반"));
    }
}
