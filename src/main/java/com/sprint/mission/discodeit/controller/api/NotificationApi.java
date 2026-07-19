package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.NotificationExamples;
import com.sprint.mission.discodeit.dto.notification.NotificationResponse;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Notification", description = "Notification API")
public interface NotificationApi {

  @Operation(summary = "Find all notifications for the current user")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Notifications retrieved successfully",
          content = @Content(mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = NotificationResponse.class)),
              examples = @ExampleObject(value = NotificationExamples.FIND_ALL_200))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Authentication required",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = NotificationExamples.ERROR_401_AUTH_003))
      )
  })
  ResponseEntity<List<NotificationResponse>> findAll(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  );

  @Operation(summary = "Delete notification")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "Notification deleted successfully"
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Authentication required",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = NotificationExamples.ERROR_401_AUTH_003))
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Notification does not belong to the requester",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = NotificationExamples.ERROR_403_AUTH_004))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Notification not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = NotificationExamples.ERROR_404_NOTIFICATION_001))
      )
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "Notification ID") @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  );
}