package com.sprint.mission.discodeit.retry;

import com.sprint.mission.discodeit.config.MDCLoggingInterceptor;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 비동기 로직의 재시도가 모두 실패했을 때, 그 사실을 관리자 계정에게 알림으로 통지하는 컴포넌트입니다.
 * 디버깅에 필요한 정보(요청 ID, 대상 리소스 ID, 실패 원인)를 알림 내용에 포함합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AdminFailureNotifier {

  private final NotificationService notificationService;
  private final UserRepository userRepository;

  @Value("${discodeit.admin.username}")
  private String adminUsername;

  public void notifyBinaryContentFailure(String taskName, UUID binaryContentId, Throwable error) {
    String requestId = MDC.get(MDCLoggingInterceptor.REQUEST_ID);
    String title = taskName + " 실패";
    String content = "RequestId: " + requestId + "\n"
        + "BinaryContentId: " + binaryContentId + "\n"
        + "Error: " + error.getMessage();

    userRepository.findByUsername(adminUsername)
        .map(User::getId)
        .ifPresentOrElse(
            adminId -> notificationService.create(adminId, title, content),
            () -> log.warn(
                "관리자 계정을 찾을 수 없어 실패 알림을 생성하지 못했습니다. binaryContentId={}, requestId={}",
                binaryContentId, requestId)
        );
  }
}
