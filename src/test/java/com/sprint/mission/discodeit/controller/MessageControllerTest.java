package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void create_success() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Instant now = Instant.now();

        MessageDto response = new MessageDto(
                messageId,
                now,
                now,
                "hello",
                channelId,
                null,
                List.of()
        );

        MockMultipartFile messageCreateRequest = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "content": "hello",
                  "channelId": "%s",
                  "authorId": "%s"
                }
                """.formatted(channelId, authorId).getBytes(StandardCharsets.UTF_8)
        );

        given(messageService.create(any(), any()))
                .willReturn(response);

        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.attachments").isArray());

        then(messageService).should().create(any(), any());
    }

    @Test
    void create_fail_whenChannelIdMissing() throws Exception {
        UUID authorId = UUID.randomUUID();

        MockMultipartFile messageCreateRequest = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "content": "hello",
                  "authorId": "%s"
                }
                """.formatted(authorId).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("ValidationError"))
                .andExpect(jsonPath("$.details.channelId").exists());

        then(messageService).should(never()).create(any(), any());
    }

    @Test
    void update_success() throws Exception {
        UUID messageId = UUID.randomUUID();

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newContent": "updated message"
                        }
                        """))
                .andExpect(status().isOk());

        then(messageService).should().update(eq(messageId), any());
    }

    @Test
    void update_fail_whenMessageNotFound() throws Exception {
        UUID messageId = UUID.randomUUID();

        willThrow(new MessageNotFoundException(messageId))
                .given(messageService)
                .update(eq(messageId), any());

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newContent": "updated message"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.exceptionType").value("MessageNotFoundException"));

        then(messageService).should().update(eq(messageId), any());
    }

    @Test
    void delete_success() throws Exception {
        UUID messageId = UUID.randomUUID();

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());

        then(messageService).should().delete(messageId);
    }

    @Test
    void delete_fail_whenMessageNotFound() throws Exception {
        UUID messageId = UUID.randomUUID();

        willThrow(new MessageNotFoundException(messageId))
                .given(messageService)
                .delete(messageId);

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.exceptionType").value("MessageNotFoundException"));

        then(messageService).should().delete(messageId);
    }

    @Test
    void findAllByChannelId_success() throws Exception {
        UUID channelId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        Instant now = Instant.now();

        MessageDto message = new MessageDto(
                messageId,
                now,
                now,
                "hello",
                channelId,
                null,
                List.of()
        );

        PageResponse<MessageDto> response = new PageResponse<>(
                List.of(message),
                null,
                1,
                false,
                null
        );

        given(messageService.findAllByChannelId(eq(channelId), any()))
                .willReturn(response);

        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(messageId.toString()))
                .andExpect(jsonPath("$.content[0].content").value("hello"))
                .andExpect(jsonPath("$.content[0].channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));

        then(messageService).should().findAllByChannelId(eq(channelId), any());
    }

    @Test
    void findAllByChannelId_fail_whenChannelNotFound() throws Exception {
        UUID channelId = UUID.randomUUID();

        willThrow(new ChannelNotFoundException(channelId))
                .given(messageService)
                .findAllByChannelId(eq(channelId), any());

        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.exceptionType").value("ChannelNotFoundException"));

        then(messageService).should().findAllByChannelId(eq(channelId), any());
    }
}
