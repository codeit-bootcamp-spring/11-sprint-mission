package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MessageService messageService;

    private MessageDto messageDto(String content) {
        UserDto author = new UserDto(UUID.randomUUID(), "author", "author@test.com", null, true);
        return new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), content,
            UUID.randomUUID(), author, List.of());
    }

    @Test
    void create_정상_201반환() throws Exception {
        MessageCreateRequest request = new MessageCreateRequest(
            "안녕하세요", UUID.randomUUID(), UUID.randomUUID()
        );
        given(messageService.create(any(), any())).willReturn(messageDto("안녕하세요"));

        MockMultipartFile messagePart = new MockMultipartFile(
            "messageCreateRequest", "", "application/json",
            objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/messages").file(messagePart))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("안녕하세요"));
    }

    @Test
    void update_정상_200반환() throws Exception {
        UUID messageId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest("수정된 메시지");
        given(messageService.update(eq(messageId), any())).willReturn(messageDto("수정된 메시지"));

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("수정된 메시지"));
    }

    @Test
    void update_유효성실패_400반환() throws Exception {
        UUID messageId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest("");

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void delete_정상_204반환() throws Exception {
        UUID messageId = UUID.randomUUID();
        willDoNothing().given(messageService).delete(messageId);

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_없는메시지_404반환() throws Exception {
        UUID messageId = UUID.randomUUID();
        willThrow(new MessageNotFoundException()).given(messageService).delete(messageId);

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
    }

    @Test
    void findAllByChannelId_정상_200반환() throws Exception {
        UUID channelId = UUID.randomUUID();
        PageResponse<MessageDto> page = new PageResponse<>(
            List.of(messageDto("msg1"), messageDto("msg2")), null, 2, false, 2L
        );
        given(messageService.findAllByChannelId(eq(channelId), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.hasNext").value(false));
    }
}