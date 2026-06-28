package com.sprint.mission.discodeit.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageWithoutChannelAccessException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser(roles = {"CHANNEL_MANAGER"})
class MessageApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private ChannelService channelService;

  @Autowired
  private MessageService messageService;

  private UUID userId;
  private UUID channelId;
  private UUID messageId;
  private String content;
  private DiscodeitUserDetails testerDetails;

  @BeforeEach
  void setUp() {
    UserResponse user = userService.createUser(
        new UserCreateRequest("tester", "tester@example.io", "password1234"),
        Optional.empty()
    );
    userId = user.id();
    testerDetails = new DiscodeitUserDetails(user, "irrelevant");

    // Temporarily set CHANNEL_MANAGER context for secured service calls in setUp
    var tempAuth = new UsernamePasswordAuthenticationToken(
        "tester", null, List.of(new SimpleGrantedAuthority("ROLE_CHANNEL_MANAGER")));
    SecurityContextHolder.getContext().setAuthentication(tempAuth);

    ChannelResponse channel = channelService.createPublicChannel(
        new PublicChannelCreateRequest("general", "description")
    );
    channelId = channel.id();

    content = "hello";
    MessageResponse message = messageService.createMessage(
        new MessageCreateRequest(content, channelId, userId),
        List.of()
    );
    messageId = message.id();

    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("success")
    void create_success() throws Exception {
      // given
      MessageCreateRequest request = new MessageCreateRequest("new message", channelId, userId);
      MockMultipartFile requestPart = new MockMultipartFile(
          "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then
      mockMvc.perform(multipart("/api/messages")
              .file(requestPart)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("new message"))
          .andExpect(jsonPath("$.channelId").value(channelId.toString()));
    }

    @Test
    @DisplayName("fail with message without channel access")
    void create_fail_message_without_channel_access_throws_exception() throws Exception {
      // given - user is not a member of this private channel
      var tempAuth = new UsernamePasswordAuthenticationToken(
          "tester", null, List.of(new SimpleGrantedAuthority("ROLE_CHANNEL_MANAGER")));
      SecurityContextHolder.getContext().setAuthentication(tempAuth);
      UserResponse otherUser = userService.createUser(
          new UserCreateRequest("other", "other@example.io", "password1234"),
          Optional.empty()
      );
      ChannelResponse privateChannel = channelService.createPrivateChannel(
          new PrivateChannelCreateRequest(List.of(otherUser.id()))
      );
      SecurityContextHolder.clearContext();

      MessageCreateRequest request = new MessageCreateRequest("hello", privateChannel.id(), userId);
      MockMultipartFile requestPart = new MockMultipartFile(
          "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then
      ErrorCode errorCode = ErrorCode.MESSAGE_WITHOUT_CHANNEL_ACCESS;

      mockMvc.perform(multipart("/api/messages")
              .file(requestPart)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.userId").value(userId.toString()))
          .andExpect(jsonPath("$.details.channelId").value(privateChannel.id().toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(MessageWithoutChannelAccessException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("success")
    void update_success() throws Exception {
      // given
      String newContent = "updated content";
      MessageUpdateRequest request = new MessageUpdateRequest(newContent);
      MockMultipartFile requestPart = new MockMultipartFile(
          "messageUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then
      mockMvc.perform(multipart("/api/messages/{messageId}", messageId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(messageId.toString()))
          .andExpect(jsonPath("$.content").value(newContent));
    }

    @Test
    @DisplayName("fail with message not found")
    void update_fail_message_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();
      MessageUpdateRequest request = new MessageUpdateRequest("updated");
      MockMultipartFile requestPart = new MockMultipartFile(
          "messageUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then
      ErrorCode errorCode = ErrorCode.MESSAGE_NOT_FOUND;

      mockMvc.perform(multipart("/api/messages/{messageId}", nonExistentId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.messageId").value(nonExistentId.toString()))
          .andExpect(
              jsonPath("$.exceptionType").value(MessageNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("success")
    void delete_success() throws Exception {
      // when & then
      mockMvc.perform(delete("/api/messages/{messageId}", messageId)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("fail with message not found")
    void delete_fail_message_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();

      // when & then
      ErrorCode errorCode = ErrorCode.MESSAGE_NOT_FOUND;

      mockMvc.perform(delete("/api/messages/{messageId}", nonExistentId)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.messageId").value(nonExistentId.toString()))
          .andExpect(
              jsonPath("$.exceptionType").value(MessageNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("findAllByChannelId")
  class FindAllByChannelId {

    @Test
    @DisplayName("success")
    void findAllByChannelId_success() throws Exception {
      // when & then
      mockMvc.perform(get("/api/messages")
              .with(user(testerDetails))
              .param("channelId", channelId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1))
          .andExpect(jsonPath("$.content[0].id").value(messageId.toString()))
          .andExpect(jsonPath("$.content[0].content").value(content))
          .andExpect(jsonPath("$.hasNext").value(false));
    }
  }
}
