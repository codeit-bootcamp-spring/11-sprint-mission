package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/api/notifications")
    public List<NotificationDto> findAll(@AuthenticationPrincipal DiscodeitUserDetails userDetails) {
        return notificationService.findAllByReceiverId(userDetails.getUserDto().id());
    }

    @DeleteMapping("/api/notifications/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal DiscodeitUserDetails userDetails
    ) {
        notificationService.delete(notificationId, userDetails.getUserDto().id());
    }
}
