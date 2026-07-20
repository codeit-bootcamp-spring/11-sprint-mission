package com.sprint.mission.discodeit.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminFailureNotifierTest {

  private static final String ADMIN_USERNAME = "admin";

  @Mock
  private NotificationService notificationService;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private AdminFailureNotifier adminFailureNotifier;

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  @DisplayName("관리자 계정을 찾으면 실패 정보를 담아 알림을 생성한다")
  void notifyBinaryContentFailure_CreatesNotification_WhenAdminExists() {
    // given
    ReflectionTestUtils.setField(adminFailureNotifier, "adminUsername", ADMIN_USERNAME);

    User admin = new User(ADMIN_USERNAME, "admin@example.com", "password", null);
    UUID adminId = UUID.randomUUID();
    ReflectionTestUtils.setField(admin, "id", adminId);

    given(userRepository.findByUsername(ADMIN_USERNAME)).willReturn(Optional.of(admin));

    MDC.put("requestId", "test-request-id");
    UUID binaryContentId = UUID.randomUUID();
    Exception error = new RuntimeException("The AWS Access Key Id you provided does not exist");

    // when
    adminFailureNotifier.notifyBinaryContentFailure("S3 파일 업로드", binaryContentId, error);

    // then
    ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
    verify(notificationService).create(eq(Set.of(adminId)), eq("S3 파일 업로드 실패"),
        contentCaptor.capture());

    String content = contentCaptor.getValue();
    assertThat(content).contains("RequestId: test-request-id");
    assertThat(content).contains("BinaryContentId: " + binaryContentId);
    assertThat(content).contains("Error: The AWS Access Key Id you provided does not exist");
  }

  @Test
  @DisplayName("관리자 계정을 찾을 수 없으면 알림을 생성하지 않는다")
  void notifyBinaryContentFailure_DoesNothing_WhenAdminNotFound() {
    // given
    ReflectionTestUtils.setField(adminFailureNotifier, "adminUsername", ADMIN_USERNAME);
    given(userRepository.findByUsername(ADMIN_USERNAME)).willReturn(Optional.empty());

    UUID binaryContentId = UUID.randomUUID();
    Exception error = new RuntimeException("boom");

    // when
    adminFailureNotifier.notifyBinaryContentFailure("S3 파일 업로드", binaryContentId, error);

    // then
    verify(notificationService, never()).create(any(), anyString(), anyString());
  }
}
