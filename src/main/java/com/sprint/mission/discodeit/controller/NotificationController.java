package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.basic.BasicNotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final BasicNotificationService notificationService;


  @GetMapping
  public ResponseEntity<List<NotificationDto>> findAll(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails // 프로젝트 방식에 맞게 교체
  ) {
    List<NotificationDto> notifications =
        notificationService.findAll(userDetails.getUserDto().id());
    return ResponseEntity.ok(notifications);
  }


  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> confirm(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  ) {
    notificationService.delete(notificationId, userDetails.getUserDto().id());
    return ResponseEntity.noContent().build();
  }

}
