package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.ChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
@Import(GlobalExceptionHandler.class)
class ChannelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChannelService channelService;

    @Test
    @DisplayName("PUBLIC 채널을 생성할 수 있다")
    void createPublic_success() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();

        ChannelDto response = new ChannelDto(
                channelId,
                "general",
                "general channel",
                ChannelType.PUBLIC,
                List.of(),
                null
        );

        given(channelService.createPublic(any())).willReturn(response);

        // when & then
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
                .andExpect(jsonPath("$.name").value("general"))
                .andExpect(jsonPath("$.description").value("general channel"))
                .andExpect(jsonPath("$.type").value("PUBLIC"));

        then(channelService).should().createPublic(any());
    }

    @Test
    @DisplayName("PUBLIC 채널 생성 시 name이 비어 있으면 400을 반환한다")
    void createPublic_fail_whenNameBlank() throws Exception {
        // when & then
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "",
                          "description": "general channel"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.name").exists());

        then(channelService).should(never()).createPublic(any());
    }

    @Test
    @DisplayName("PRIVATE 채널을 생성할 수 있다")
    void createPrivate_success() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        ChannelDto response = new ChannelDto(
                channelId,
                null,
                null,
                ChannelType.PRIVATE,
                List.of(),
                null
        );

        given(channelService.createPrivate(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "participantIds": ["%s", "%s"]
                        }
                        """.formatted(userId1, userId2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value("PRIVATE"));

        then(channelService).should().createPrivate(any());
    }

    @Test
    @DisplayName("사용자별 채널 목록을 조회할 수 있다")
    void findAllByUserId_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        ChannelDto channelDto = new ChannelDto(
                channelId,
                "general",
                "general channel",
                ChannelType.PUBLIC,
                List.of(),
                null
        );

        given(channelService.findAllByUserId(userId)).willReturn(List.of(channelDto));

        // when & then
        mockMvc.perform(get("/api/channels")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(channelId.toString()))
                .andExpect(jsonPath("$[0].name").value("general"))
                .andExpect(jsonPath("$[0].type").value("PUBLIC"));

        then(channelService).should().findAllByUserId(userId);
    }

    @Test
    @DisplayName("채널을 수정할 수 있다")
    void update_success() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();

        ChannelDto response = new ChannelDto(
                channelId,
                "new-name",
                "new-description",
                ChannelType.PUBLIC,
                List.of(),
                null
        );

        given(channelService.update(any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newName": "new-name",
                          "newDescription": "new-description"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.name").value("new-name"))
                .andExpect(jsonPath("$.description").value("new-description"));

        then(channelService).should().update(any());
    }

    @Test
    @DisplayName("채널을 삭제할 수 있다")
    void delete_success() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());

        then(channelService).should().delete(eq(channelId));
    }
}