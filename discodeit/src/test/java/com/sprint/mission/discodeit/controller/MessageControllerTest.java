package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.service.dto.message.MessageDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MessageService messageService;

    @Test
    void findAllByChannelId_성공_200() throws Exception {
        UUID channelId = UUID.randomUUID();
        MessageDto message = MessageDto.builder()
                .id(UUID.randomUUID())
                .content("안녕하세요")
                .channelId(channelId)
                .createdAt(Instant.now())
                .attachments(List.of())
                .build();
        PageResponse<MessageDto> pageResponse = PageResponse.<MessageDto>builder()
                .content(List.of(message))
                .size(50)
                .hasNext(false)
                .build();

        given(messageService.findAllByChannelId(any(), any(), any(Integer.class))).willReturn(pageResponse);

        mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].content").value("안녕하세요"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void findAllByChannelId_채널ID_없음_400() throws Exception {
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_성공_201() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        CreateMessageRequest request = new CreateMessageRequest(authorId, channelId, "안녕하세요", List.of());
        MessageDto response = MessageDto.builder()
                .id(UUID.randomUUID())
                .content("안녕하세요")
                .channelId(channelId)
                .createdAt(Instant.now())
                .attachments(List.of())
                .build();

        given(messageService.create(any(CreateMessageRequest.class), any())).willReturn(response);

        MockMultipartFile messagePart = new MockMultipartFile(
                "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/messages").file(messagePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("안녕하세요"));
    }

    @Test
    void update_성공_200() throws Exception {
        UUID messageId = UUID.randomUUID();
        MessageDto response = MessageDto.builder()
                .id(messageId)
                .content("수정된 내용")
                .createdAt(Instant.now())
                .attachments(List.of())
                .build();

        given(messageService.update(any())).willReturn(response);

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newContent\":\"수정된 내용\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    void update_내용_없음_400() throws Exception {
        UUID messageId = UUID.randomUUID();

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newContent\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details.newContent").exists());
    }

    @Test
    void delete_성공_204() throws Exception {
        UUID messageId = UUID.randomUUID();
        willDoNothing().given(messageService).delete(messageId);

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_메시지_없음_404() throws Exception {
        UUID messageId = UUID.randomUUID();
        willThrow(new DiscodeitException(ErrorCode.MESSAGE_NOT_FOUND)).given(messageService).delete(messageId);

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
    }
}
