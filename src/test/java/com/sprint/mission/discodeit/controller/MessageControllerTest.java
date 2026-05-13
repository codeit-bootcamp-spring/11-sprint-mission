package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@Import(GlobalExceptionHandler.class)
class MessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MessageService messageService;

    @Test
    @DisplayName("multipart 요청으로 메시지를 생성할 수 있다")
    void create_success() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        UserDto author = new UserDto(
                authorId,
                "evan",
                "evan@test.com",
                null,
                true
        );

        MessageDto response = new MessageDto(
                messageId,
                Instant.parse("2026-05-09T10:00:00Z"),
                Instant.parse("2026-05-09T10:00:00Z"),
                "hello",
                channelId,
                author,
                List.of()
        );

        given(messageService.create(any())).willReturn(response);

        MockMultipartFile messageCreateRequest = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "authorId": "%s",
                  "channelId": "%s",
                  "content": "hello"
                }
                """.formatted(authorId, channelId).getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.author.id").value(authorId.toString()))
                .andExpect(jsonPath("$.author.username").value("evan"));

        then(messageService).should().create(any());
    }

    @Test
    @DisplayName("메시지 생성 시 content가 비어 있으면 400을 반환한다")
    void create_fail_whenContentBlank() throws Exception {
        // given
        UUID authorId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        MockMultipartFile messageCreateRequest = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "authorId": "%s",
                  "channelId": "%s",
                  "content": ""
                }
                """.formatted(authorId, channelId).getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.content").exists());

        then(messageService).should(never()).create(any());
    }

    @Test
    @DisplayName("채널별 메시지 목록을 조회할 수 있다")
    void findAllByChannelId_success() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        UserDto author = new UserDto(
                authorId,
                "evan",
                "evan@test.com",
                null,
                true
        );

        MessageDto messageDto = new MessageDto(
                messageId,
                Instant.parse("2026-05-09T10:00:00Z"),
                Instant.parse("2026-05-09T10:00:00Z"),
                "hello",
                channelId,
                author,
                List.of()
        );

        PageResponse<MessageDto> response = new PageResponse<>(
                List.of(messageDto),
                null,
                50
        );

        given(messageService.findAllByChannelId(channelId, null, 50)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString())
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(messageId.toString()))
                .andExpect(jsonPath("$.content[0].content").value("hello"))
                .andExpect(jsonPath("$.content[0].channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.size").value(50));

        then(messageService).should().findAllByChannelId(channelId, null, 50);
    }

    @Test
    @DisplayName("메시지를 수정할 수 있다")
    void update_success() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        MessageDto response = new MessageDto(
                messageId,
                Instant.parse("2026-05-09T10:00:00Z"),
                Instant.parse("2026-05-09T10:01:00Z"),
                "updated",
                channelId,
                null,
                List.of()
        );

        given(messageService.update(any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newContent": "updated"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value("updated"));

        then(messageService).should().update(any());
    }

    @Test
    @DisplayName("메시지 수정 시 newContent가 비어 있으면 400을 반환한다")
    void update_fail_whenContentBlank() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newContent": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.newContent").exists());

        then(messageService).should(never()).update(any());
    }

    @Test
    @DisplayName("메시지를 삭제할 수 있다")
    void delete_success() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());

        then(messageService).should().delete(eq(messageId));
    }
}