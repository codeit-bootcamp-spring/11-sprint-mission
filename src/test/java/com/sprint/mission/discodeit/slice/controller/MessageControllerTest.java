package com.sprint.mission.discodeit.slice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sprint.mission.discodeit.controller.MessageController;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Gson gson;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Test
  @DisplayName("메시지 생성 성공")
  void create_success_message() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    MessageCreateRequest request = new MessageCreateRequest("메시지", channelId, userId);

    Channel channel = mock(Channel.class);
    User user = mock(User.class);
    UserDto userDto = new UserDto(
        user.getId(),
        "test",
        "test@naver.com",
        null,
        true,
        Role.USER
    );

    Message message = Message.create("메시지", channel, user);
    MessageDto dto = new MessageDto(
        message.getId(),
        message.getCreatedAt(),
        message.getUpdatedAt(),
        message.getContent(),
        channel.getId(),
        userDto,
        List.of());

    given(messageService.create(any(MessageCreateRequest.class), any())).willReturn(dto);

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", "application/json",
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("메시지"));
  }

  @Test
  @DisplayName("메시지 생성 실패(채널이 존재하지 않음)")
  void create_fail_message_notfound_channel() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    MessageCreateRequest request = new MessageCreateRequest("메시지", channelId, userId);

    Channel channel = mock(Channel.class);
    User user = mock(User.class);
    Message message = Message.create("메시지", channel, user);

    given(messageService.create(any(MessageCreateRequest.class), any())).willThrow(
        new ChannelNotFoundException(channelId));

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", "application/json",
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("메시지 수정 성공")
  void update_success_message() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    UserDto userDto = mock(UserDto.class);

    MessageUpdateRequest request = new MessageUpdateRequest("수정 메시지");
    Message message = Message.create(request.newContent(), mock(Channel.class), mock(User.class));
    MessageDto dto = new MessageDto(
        message.getId(),
        message.getCreatedAt(),
        message.getUpdatedAt(),
        message.getContent(),
        channelId,
        userDto,
        List.of());

    given(messageService.update(any(), any())).willReturn(dto);

    // when & then
    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정 메시지"));
  }

  @Test
  @DisplayName("메시지 수정 실패(메시지가 존재하지 않음")
  void update_fail_message_notfound_message() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();

    MessageUpdateRequest request = new MessageUpdateRequest("수정 메시지");

    given(messageService.update(any(), any())).willThrow(new MessageNotFoundException(messageId));

    // when & then
    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("메시지 삭제 성공")
  void delete_success_message() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("메시지 삭제 실패(메시지가 존재하지 않음)")
  void delete_fail_message_notfound_message() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();

    willThrow(new MessageNotFoundException(messageId)).given(messageService).delete(messageId);

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNotFound());
  }

}
