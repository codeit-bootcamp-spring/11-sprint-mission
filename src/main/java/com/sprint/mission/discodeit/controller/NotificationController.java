package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.controller.api.NotificationApi;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EndPoints.NOTIFICATION)
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

  private final NotificationService notificationService;

  @Override
  @GetMapping
  public ResponseEntity<List<NotificationDto>> findAll(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  ) {
    List<NotificationDto> response = notificationService.findAll(userDetails.getUserDto().id());

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @Override
  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  ) {
    notificationService.delete(notificationId, userDetails.getUserDto().id());

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
