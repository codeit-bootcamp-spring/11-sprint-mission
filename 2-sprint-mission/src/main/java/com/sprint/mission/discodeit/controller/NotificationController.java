package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationDto.Response>> findAll(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    UUID receiverId = userDetails.getUserDto().id();
    log.debug("알림 목록 조회 요청: receiverId={}", receiverId);

    List<NotificationDto.Response> responses =
        notificationService.findAllByReceiverId(receiverId);

    log.debug("알림 목록 조회 응답: {}건", responses.size());
    return ResponseEntity.ok(responses);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    log.info("알림 삭제 요청: id={}", notificationId);

    notificationService.delete(notificationId, userDetails.getUserDto().id());

    return ResponseEntity.noContent().build();
  }
}