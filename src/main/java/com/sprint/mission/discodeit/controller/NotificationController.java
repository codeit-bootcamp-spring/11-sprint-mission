package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.NotificationApi;
import com.sprint.mission.discodeit.dto.notification.NotificationResponse;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@RestController
public class NotificationController implements NotificationApi {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationResponse>> findAll(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    log.info("notification find-all request: receiverId={}", userDetails.getUser().id());
    List<NotificationResponse> notificationResponses = this.notificationService.findAllByReceiverId(
        userDetails.getUser().id());

    log.debug("notification find-all response: count={}", notificationResponses.size());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(notificationResponses);
  }

  @DeleteMapping(path = "{notificationId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    log.info("notification delete request: id={}, receiverId={}", notificationId,
        userDetails.getUser().id());
    this.notificationService.deleteNotification(notificationId, userDetails.getUser().id());

    log.debug("notification delete response: no-content");
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}