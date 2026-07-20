package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ChannelIntegrationTest {

  private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
  private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private Channel savedPublicChannel;
  private Channel savedPrivateChannel;
  private User savedUser;
  private String accessToken;
  private String csrfToken;
  private Cookie csrfCookie;

  @BeforeEach
  void setUp() throws Exception {
    User user = new User("testUser", "test@test.com", passwordEncoder.encode("password123"), null);
    user.updateRole(Role.CHANNEL_MANAGER);
    savedUser = userRepository.save(user);

    savedPublicChannel = channelRepository.save(
        new Channel("public", "test channel", ChannelType.PUBLIC)
    );
    savedPrivateChannel = channelRepository.save(
        new Channel("", "", ChannelType.PRIVATE)
    );

    MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf-token"))
        .andExpect(status().isNoContent())
        .andReturn();
    csrfCookie = csrfResult.getResponse().getCookie(CSRF_COOKIE_NAME);
    csrfToken = csrfCookie.getValue();

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .param("username", "testUser")
            .param("password", "password123")
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    accessToken = json.get("accessToken").asText();
  }

  // --- POST(PUBLIC) ---
  @Test
  @DisplayName("유효한 요청으로 Public 채널을 생성 시 201과 채널 정보를 반환")
  void createPublicChannel_returns201() throws Exception {
    PublicChannelRequest request = new PublicChannelRequest("test", "des");

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("test"))
        .andExpect(jsonPath("$.type").value("PUBLIC"))
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  @DisplayName("채널명이 한글자일 경우 Public 채널 생성 시 400을 반환")
  void createPublicChannel_shortName_returns400() throws Exception {
    PublicChannelRequest request = new PublicChannelRequest("a", "des");

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  // --- POST(PRIVATE) ---
  @Test
  @DisplayName("유효한 요청으로 Private 채널을 생성 시 201과 채널 정보를 반환")
  void createPrivateChannel_returns201() throws Exception {
    PrivateChannelRequest request = new PrivateChannelRequest(List.of(savedUser.getId()));

    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andDo(print())
        .andExpect(jsonPath("$.type").value("PRIVATE"))
        .andExpect(jsonPath("$.participants.length()").value(1));
  }

  @Test
  @DisplayName("존재하지 않는 유저 ID로 Private채널 생성 시 404를 반환")
  void createPrivateChannel_userNotFound_returns404() throws Exception {
    PrivateChannelRequest request = new PrivateChannelRequest(List.of(UUID.randomUUID()));

    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }

  // --- DELETE ---
  @Test
  @DisplayName("존재하는 채널 삭제 시 204를 반환")
  void deleteChannel_returns204() throws Exception {
    mockMvc.perform(delete("/api/channels/{channelId}", savedPublicChannel.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("존재하지 않는 채널 삭제 시 404와 CHANNEL_NOT_FOUND를 반환")
  void deleteChannel_notFound_returns404() throws Exception {
    mockMvc.perform(delete("/api/channels/{channelId}", UUID.randomUUID())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
  }

  // --- PATCH ---
  @Test
  @DisplayName("Public 채널 수정 시 200과 수정된 채널 정보를 반환")
  void updatePublicChannel_returns200() throws Exception {
    ChannelUpdateRequest request = new ChannelUpdateRequest("updateName", "updateDes");

    mockMvc.perform(patch("/api/channels/{channelId}", savedPublicChannel.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("updateName"))
        .andExpect(jsonPath("$.description").value("updateDes"));
  }

  @Test
  @DisplayName("Private 채널 수정 시 400과 CHANNEL_MODIFY_PRIVATE를 반환")
  void updatePrivateChannel_returns400() throws Exception {
    ChannelUpdateRequest request = new ChannelUpdateRequest("newName", null);

    mockMvc.perform(patch("/api/channels/{channelId}", savedPrivateChannel.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .cookie(csrfCookie)
            .header(CSRF_HEADER_NAME, csrfToken))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CHANNEL_MODIFY_PRIVATE"));
  }
}
