package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface NotificationApi {

  @GetMapping
  ResponseEntity<List<NotificationDto>> findAll(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  );

  @DeleteMapping("/{notificationId}")
  ResponseEntity<Void> delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  );

}
