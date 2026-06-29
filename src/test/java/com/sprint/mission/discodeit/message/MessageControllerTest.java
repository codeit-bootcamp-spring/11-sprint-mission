package com.sprint.mission.discodeit.message;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.MessageController;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  @Test
  @DisplayName("메시지 생성 성공 - 201 응답 반환")
  void create_success() throws Exception {
    //given
    MessageCreateRequest request = new MessageCreateRequest(UUID.randomUUID(), UUID.randomUUID(),
        "안녕");
    MessageDto dto = new MessageDto(UUID.randomUUID(), null, null, null, UUID.randomUUID(), "안녕",
        null);

    given(messageService.create(any(MessageCreateRequest.class), any())).willReturn(dto);

    MockMultipartFile messageCreateRequest = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    //when & then
    mockMvc.perform(
            multipart("/api/messages")
                .file(messageCreateRequest)
        )
        .andDo(print())
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("메시지 생성 실패 - Channel 또는 User를 찾을 수 없을 시 404 Not Found 응답 반환")
  void create_fail_not_found() throws Exception {
    //given
    MessageCreateRequest request = new MessageCreateRequest(UUID.randomUUID(), UUID.randomUUID(),
        "안녕");
    given(messageService.create(any(MessageCreateRequest.class), any()))
        .willThrow(new UserNotFoundException(
            request.authorId())); // 이 테스트의 본질은 응답 결과를 잘 주는지만 확인하는 것이므로 대놓고 에러를 던짐

    MockMultipartFile messageCreateRequest = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    //when & then
    mockMvc.perform(
            multipart("/api/messages")
                .file(messageCreateRequest)
        )
        .andDo(print())
        .andExpect(status().isNotFound());
  }
}
