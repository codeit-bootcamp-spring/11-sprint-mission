package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
public class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChannelService channelService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createPublicChannel_success() throws Exception {
        UUID channelId = UUID.randomUUID();

        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "general",
                "general channel",
                List.of(),
                null
        );

        given(channelService.createPublicChannel(any()))
                .willReturn(response);

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "general",
                          "description": "general channel"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andExpect(jsonPath("$.name").value("general"))
                .andExpect(jsonPath("$.description").value("general channel"));

        then(channelService).should().createPublicChannel(any());
    }

    @Test
    void createPublicChannel_fail_whenNameBlank() throws Exception {
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "",
                          "description": "general channel"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("ValidationError"))
                .andExpect(jsonPath("$.details.name").exists());

        then(channelService).should(never()).createPublicChannel(any());
    }

    @Test
    void createPrivateChannel_success() throws Exception {
        UUID channelId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PRIVATE,
                null,
                null,
                List.of(),
                null
        );

        given(channelService.createPrivateChannel(any()))
                .willReturn(response);

        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "participantIds": ["%s", "%s"]
                        }
                        """.formatted(userId1, userId2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value("PRIVATE"))
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.description").doesNotExist());

        then(channelService).should().createPrivateChannel(any());
    }

    @Test
    void createPrivateChannel_fail_whenParticipantIdsEmpty() throws Exception {
        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "participantIds": []
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("ValidationError"))
                .andExpect(jsonPath("$.details.participantIds").exists());

        then(channelService).should(never()).createPrivateChannel(any());
    }

    @Test
    void update_success() throws Exception {
        UUID channelId = UUID.randomUUID();

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newName": "notice",
                          "newDescription": "notice channel"
                        }
                        """))
                .andExpect(status().isOk());

        then(channelService).should().update(any(), any());
    }

    @Test
    void update_fail_whenChannelNotFound() throws Exception {
        UUID channelId = UUID.randomUUID();

        willThrow(new ChannelNotFoundException(channelId))
                .given(channelService)
                .update(any(), any());

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newName": "notice",
                          "newDescription": "notice channel"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.exceptionType").value("ChannelNotFoundException"));

        then(channelService).should().update(any(), any());
    }

    @Test
    void delete_success() throws Exception {
        UUID channelId = UUID.randomUUID();

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());

        then(channelService).should().delete(channelId);
    }

    @Test
    void delete_fail_whenChannelNotFound() throws Exception {
        UUID channelId = UUID.randomUUID();

        willThrow(new ChannelNotFoundException(channelId))
                .given(channelService)
                .delete(channelId);

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.exceptionType").value("ChannelNotFoundException"));

        then(channelService).should().delete(channelId);
    }

    @Test
    void findAllByUserId_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        ChannelDto channel = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "general",
                "general channel",
                List.of(),
                null
        );

        given(channelService.findAllByUserId(userId))
                .willReturn(List.of(channel));

        mockMvc.perform(get("/api/channels")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(channelId.toString()))
                .andExpect(jsonPath("$[0].type").value("PUBLIC"))
                .andExpect(jsonPath("$[0].name").value("general"));

        then(channelService).should().findAllByUserId(userId);
    }
}
