package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChannelIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private EntityManager entityManager;

  private User saveTestUser(String username, String email) {
    BinaryContent profile = BinaryContent.builder()
        .fileName("profile.jpg")
        .size(1024L)
        .contentType("image/jpeg")
        .build();
    entityManager.persist(profile);

    User user = User.builder()
        .username(username)
        .email(email)
        .password("Password123!")
        .profile(profile)
        .build();
    entityManager.persist(user);
    return user;
  }

  private Channel saveTestPublicChannel(String name, String description) {
    Channel channel = Channel.createPublic(name, description);
    entityManager.persist(channel);
    return channel;
  }

  private Channel saveTestPrivateChannel(List<User> participants) {
    Channel channel = Channel.createPrivate();
    entityManager.persist(channel);

    for (User user : participants) {
      ReadStatus readStatus = ReadStatus.builder()
          .user(user)
          .channel(channel)
          .lastReadAt(Instant.now())
          .build();
      entityManager.persist(readStatus);
    }

    return channel;
  }

  // create(Public) 테스트
  @Test
  @DisplayName("공개 채널 생성 API 통합 테스트 - 성공")
  void createPublic_success() throws Exception {
    // Given
    ChannelDto.CreatePublicRequest request = new ChannelDto.CreatePublicRequest(
        "테스트 채널",
        "테스트 채널 설명"
    );

    // When & Then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.type").value("PUBLIC"))
        .andExpect(jsonPath("$.name").value("테스트 채널"))
        .andExpect(jsonPath("$.description").value("테스트 채널 설명"));
  }

  @Test
  @DisplayName("공개 채널 생성 실패 API 통합 테스트 - 유효하지 않은 요청")
  void createPublic_invalidRequest_returnsBadRequest() throws Exception {
    // Given
    ChannelDto.CreatePublicRequest invalidRequest = new ChannelDto.CreatePublicRequest(
        "",
        "테스트 채널 설명"
    );

    // When & Then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  // create(Private) 테스트
  @Test
  @DisplayName("비공개 채널 생성 API 통합 테스트 - 성공")
  void createPrivate_success() throws Exception {
    // Given
    User user1 = saveTestUser("user1", "user1@example.com");
    User user2 = saveTestUser("user2", "user2@example.com");
    entityManager.flush();
    entityManager.clear();

    ChannelDto.CreatePrivateRequest request = new ChannelDto.CreatePrivateRequest(
        List.of(user1.getId(), user2.getId())
    );

    // When & Then
    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.type").value("PRIVATE"))
        .andExpect(jsonPath("$.participants.size()").value(2));
  }

  // findAllByUserId 테스트
  @Test
  @DisplayName("사용자별 채널 목록 조회 API 통합 테스트 - 성공")
  void findAllByUserId_success() throws Exception {
    // Given
    User user = saveTestUser("channeluser", "channeluser@example.com");
    User otherUser = saveTestUser("otheruser", "otheruser@example.com");

    saveTestPublicChannel("공개 채널 1", "공개 채널 설명");
    saveTestPrivateChannel(List.of(user, otherUser));

    entityManager.flush();
    entityManager.clear();

    // When & Then
    mockMvc.perform(get("/api/channels")
            .param("userId", user.getId().toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(2))
        .andExpect(jsonPath("$[0].type").value("PUBLIC"))
        .andExpect(jsonPath("$[1].type").value("PRIVATE"));
  }

  @Test
  @DisplayName("사용자별 채널 목록 조회 실패 API 통합 테스트 - 필수 파라미터 누락")
  void findAllByUserId_missingUserId_returnsBadRequest() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/channels")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  // update 테스트
  @Test
  @DisplayName("채널 업데이트 API 통합 테스트 - 성공")
  void update_success() throws Exception {
    // Given
    Channel channel = saveTestPublicChannel("원본 채널", "원본 채널 설명");
    entityManager.flush();
    entityManager.clear();

    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest(
        "수정된 채널",
        "수정된 채널 설명"
    );

    // When & Then
    mockMvc.perform(patch("/api/channels/{channelId}", channel.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(channel.getId().toString()))
        .andExpect(jsonPath("$.name").value("수정된 채널"))
        .andExpect(jsonPath("$.description").value("수정된 채널 설명"));
  }

  @Test
  @DisplayName("채널 업데이트 실패 API 통합 테스트 - 존재하지 않는 채널")
  void update_nonExistingChannel_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentChannelId = UUID.randomUUID();
    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest(
        "수정된 채널",
        "수정된 채널 설명"
    );

    // When & Then
    mockMvc.perform(patch("/api/channels/{channelId}", nonExistentChannelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("채널 업데이트 실패 API 통합 테스트 - 비공개 채널 수정 제한")
  void update_privateChannel_returnsForbidden() throws Exception {
    // Given
    User user = saveTestUser("privateMember", "member@example.com");
    Channel channel = saveTestPrivateChannel(List.of(user));
    entityManager.flush();
    entityManager.clear();

    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest(
        "비공개 채널",
        "비공개 채널 수정 시도"
    );

    // When & Then
    mockMvc.perform(patch("/api/channels/{channelId}", channel.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  // delete 테스트
  @Test
  @DisplayName("채널 삭제 API 통합 테스트 - 성공")
  void delete_success() throws Exception {
    // Given
    Channel channel = saveTestPublicChannel("삭제할 채널", "삭제할 채널 설명");
    User user = saveTestUser("testuser", "testuser@example.com");
    entityManager.flush();
    entityManager.clear();

    // When
    mockMvc.perform(delete("/api/channels/{channelId}", channel.getId()))
        .andExpect(status().isNoContent());

    // Then (삭제 후 조회 검증)
    mockMvc.perform(get("/api/channels")
            .param("userId", user.getId().toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '" + channel.getId() + "')]").doesNotExist());
  }

  @Test
  @DisplayName("채널 삭제 실패 API 통합 테스트 - 존재하지 않는 채널")
  void delete_nonExistingChannel_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentChannelId = UUID.randomUUID();

    // When & Then
    mockMvc.perform(delete("/api/channels/{channelId}", nonExistentChannelId))
        .andExpect(status().isNotFound());
  }
}