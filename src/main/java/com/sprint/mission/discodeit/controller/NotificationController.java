package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(summary = "현재 로그인 사용자의 알림 목록 조회")
  @GetMapping
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "현재 로그인 사용자의 알림 목록",
          content = @Content(schema = @Schema(implementation = NotificationDto.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  public ResponseEntity<List<NotificationDto>> findAll(@AuthenticationPrincipal
  DiscodeitUserDetails userDetails) {
    UUID receiverId = userDetails.getUserDto().id();
    List<NotificationDto> notifications = notificationService.findAllByReceiverId(receiverId);
    return ResponseEntity.ok(notifications);
  }

  @Operation(summary = "알림 확인(삭제)")
  @DeleteMapping("/{notificationId}")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "알림 확인"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
      @ApiResponse(responseCode = "403", description = "인가되지 않은 요청"),
      @ApiResponse(responseCode = "404", description = "알림 없음"),
  })
  public ResponseEntity<Void> delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  ) {
    notificationService.delete(notificationId, userDetails.getUserDto().id());
    return ResponseEntity.noContent().build();
  }


}
