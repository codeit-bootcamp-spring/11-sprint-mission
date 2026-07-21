package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.util.AsyncTestUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BinaryContentApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private BinaryContentService binaryContentService;

  @Autowired
  private UserService userService;

  @Autowired
  private ChannelService channelService;

  @Autowired
  private MessageService messageService;

  @Test
  @WithMockUser(roles = "CHANNEL_MANAGER")
  @DisplayName("바이너리 컨텐츠 조회 API 통합 테스트")
  void findBinaryContent_Success() throws Exception {
    // Given
    // 테스트 바이너리 컨텐츠 생성 (메시지 첨부파일을 통해 생성)
    // 사용자 생성
    UserCreateRequest userRequest = new UserCreateRequest(
        "contentuser",
        "content@example.com",
        "Password1!"
    );
    UserDto user = userService.create(userRequest, Optional.empty());

    // 채널 생성
    PublicChannelCreateRequest channelRequest = new PublicChannelCreateRequest(
        "테스트 채널",
        "테스트 채널 설명입니다."
    );
    var channel = channelService.create(channelRequest);

    // 첨부파일이 있는 메시지 생성
    MessageCreateRequest messageRequest = new MessageCreateRequest(
        "첨부파일이 있는 메시지입니다.",
        channel.id(),
        user.id()
    );

    byte[] fileContent = "테스트 파일 내용입니다.".getBytes();
    BinaryContentCreateRequest attachmentRequest = new BinaryContentCreateRequest(
        "test.txt",
        MediaType.TEXT_PLAIN_VALUE,
        fileContent
    );

    MessageDto message = messageService.create(messageRequest, List.of(attachmentRequest));
    UUID binaryContentId = message.attachments().get(0).id();

    // When & Then
    mockMvc.perform(get("/api/binaryContents/{binaryContentId}", binaryContentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(binaryContentId.toString())))
        .andExpect(jsonPath("$.fileName", is("test.txt")))
        .andExpect(jsonPath("$.contentType", is(MediaType.TEXT_PLAIN_VALUE)))
        .andExpect(jsonPath("$.size", is(fileContent.length)));
  }

  @Test
  @WithMockUser(roles = "USER")
  @DisplayName("존재하지 않는 바이너리 컨텐츠 조회 API 통합 테스트")
  void findBinaryContent_Failure_NotFound() throws Exception {
    // Given
    UUID nonExistentBinaryContentId = UUID.randomUUID();

    // When & Then
    mockMvc.perform(get("/api/binaryContents/{binaryContentId}", nonExistentBinaryContentId))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "CHANNEL_MANAGER")
  @DisplayName("여러 바이너리 컨텐츠 조회 API 통합 테스트")
  void findAllBinaryContentsByIds_Success() throws Exception {
    // Given
    // 테스트 바이너리 컨텐츠 생성 (메시지 첨부파일을 통해 생성)
    UserCreateRequest userRequest = new UserCreateRequest(
        "contentuser2",
        "content2@example.com",
        "Password1!"
    );
    UserDto user = userService.create(userRequest, Optional.empty());

    PublicChannelCreateRequest channelRequest = new PublicChannelCreateRequest(
        "테스트 채널2",
        "테스트 채널 설명입니다."
    );
    var channel = channelService.create(channelRequest);

    MessageCreateRequest messageRequest = new MessageCreateRequest(
        "첨부파일이 있는 메시지입니다.",
        channel.id(),
        user.id()
    );

    // 첫 번째 첨부파일
    BinaryContentCreateRequest attachmentRequest1 = new BinaryContentCreateRequest(
        "test1.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "첫 번째 테스트 파일 내용입니다.".getBytes()
    );

    // 두 번째 첨부파일
    BinaryContentCreateRequest attachmentRequest2 = new BinaryContentCreateRequest(
        "test2.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "두 번째 테스트 파일 내용입니다.".getBytes()
    );

    // 첨부파일 두 개를 가진 메시지 생성
    MessageDto message = messageService.create(
        messageRequest,
        List.of(attachmentRequest1, attachmentRequest2)
    );

    List<UUID> binaryContentIds = message.attachments().stream()
        .map(BinaryContentDto::id)
        .toList();

    // When & Then
    mockMvc.perform(get("/api/binaryContents")
            .param("binaryContentIds", binaryContentIds.get(0).toString())
            .param("binaryContentIds", binaryContentIds.get(1).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[*].fileName", hasItems("test1.txt", "test2.txt")));
  }

  @Test
  @WithMockUser(roles = "CHANNEL_MANAGER")
  @DisplayName("바이너리 컨텐츠 다운로드 API 통합 테스트")
  void downloadBinaryContent_Success() throws Exception {
    // Given
    String fileContent = "다운로드 테스트 파일 내용입니다.";
    BinaryContentCreateRequest createRequest = new BinaryContentCreateRequest(
        "download-test.txt",
        MediaType.TEXT_PLAIN_VALUE,
        fileContent.getBytes()
    );

    BinaryContentDto binaryContent = binaryContentService.create(createRequest);
    UUID binaryContentId = binaryContent.id();

    // 바이너리 데이터 저장은 BinaryContentCreatedEvent 리스너가 트랜잭션 커밋 이후에 처리합니다.
    // 테스트는 클래스 전체가 트랜잭션으로 감싸져 롤백되므로, 리스너가 실행되도록
    // 여기서 명시적으로 트랜잭션을 커밋시킵니다.
    TestTransaction.flagForCommit();
    TestTransaction.end();
    TestTransaction.start();

    // 리스너가 @Async로 별도 스레드에서 처리되므로(+ 저장 로직에 3초 지연이 있으므로),
    // 상태가 SUCCESS/FAIL로 바뀔 때까지 폴링으로 기다립니다.
    AsyncTestUtils.awaitUntil(
        () -> binaryContentService.find(binaryContentId).status() != BinaryContentStatus.PROCESSING,
        8000
    );

    // When & Then
    mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", binaryContentId))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition",
            "attachment; filename=\"download-test.txt\""))
        .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
        .andExpect(content().bytes(fileContent.getBytes()));
  }

  @Test
  @WithMockUser(roles = "USER")
  @DisplayName("존재하지 않는 바이너리 컨텐츠 다운로드 API 통합 테스트")
  void downloadBinaryContent_Failure_NotFound() throws Exception {
    // Given
    UUID nonExistentBinaryContentId = UUID.randomUUID();

    // When & Then
    mockMvc.perform(
            get("/api/binaryContents/{binaryContentId}/download", nonExistentBinaryContentId))
        .andExpect(status().isNotFound());
  }
} 