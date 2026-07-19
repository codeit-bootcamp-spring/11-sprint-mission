package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
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

@Tag(name = "Notification", description = "Notification API")
public interface NotificationApi {

  @Operation(summary = "알림 목록 조회")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "알림 목록 조회 성공",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationDto.class)))
      ),
      @ApiResponse(
          responseCode = "401", description = "인증되지 않은 요청",
          content = @Content(examples = @ExampleObject(value = "Unauthorized"))
      )
  })
  ResponseEntity<List<NotificationDto>> findAllByReceiverId(
      @Parameter(hidden = true) DiscodeitUserDetails userDetails
  );

  @Operation(summary = "알림 확인(삭제)")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204", description = "알림이 성공적으로 확인(삭제)됨"
      ),
      @ApiResponse(
          responseCode = "401", description = "인증되지 않은 요청",
          content = @Content(examples = @ExampleObject(value = "Unauthorized"))
      ),
      @ApiResponse(
          responseCode = "403", description = "본인의 알림이 아님",
          content = @Content(examples = @ExampleObject(value = "Forbidden"))
      ),
      @ApiResponse(
          responseCode = "404", description = "알림을 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "Notification with id {notificationId} not found"))
      )
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "확인(삭제)할 알림 ID") UUID notificationId
  );
}
