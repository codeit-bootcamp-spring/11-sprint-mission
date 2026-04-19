package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.ChannelExamples;
import com.sprint.mission.discodeit.controller.api.examples.MessageExamples;
import com.sprint.mission.discodeit.controller.api.examples.UserExamples;
import com.sprint.mission.discodeit.dto.common.PageResponse;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Message", description = "Message API")
public interface MessageApi {

  @Operation(summary = "Create message")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Message created successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = MessageResponse.class),
              examples = @ExampleObject(value = MessageExamples.CREATE_201))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Missing content",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(name = "MESSAGE_001", value = MessageExamples.ERROR_400_MESSAGE_001))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User or channel not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "USER_011", description = "User not found", value = UserExamples.ERROR_404_USER_011),
                  @ExampleObject(name = "CHANNEL_005", description = "Channel not found", value = ChannelExamples.ERROR_404_CHANNEL_005)
              })
      ),
      @ApiResponse(
          responseCode = "422",
          description = "Sender is not a channel participant",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(name = "MESSAGE_002", value = MessageExamples.ERROR_422_MESSAGE_002))
      )
  })
  ResponseEntity<MessageResponse> create(
      @RequestPart MessageCreateRequest messageCreateRequest,
      @RequestPart(required = false) List<MultipartFile> attachments
  );

  @Operation(summary = "Update message")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Message updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = MessageResponse.class),
              examples = @ExampleObject(value = MessageExamples.UPDATE_200))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Message not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(name = "MESSAGE_003", value = MessageExamples.ERROR_404_MESSAGE_003))
      )
  })
  ResponseEntity<MessageResponse> update(
      @Parameter(description = "Message ID") @PathVariable UUID messageId,
      @RequestPart(required = false) MessageUpdateRequest messageUpdateRequest,
      @RequestPart(required = false) List<MultipartFile> attachments
  );

  @Operation(summary = "Delete message")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
      @ApiResponse(
          responseCode = "404",
          description = "Message not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(name = "MESSAGE_003", value = MessageExamples.ERROR_404_MESSAGE_003))
      )
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "Message ID") @PathVariable UUID messageId
  );

  @Operation(summary = "Find all messages by channel")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Messages retrieved successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = MessageResponse.class),
              examples = @ExampleObject(value = MessageExamples.FIND_ALL_200))
      )
  })
  ResponseEntity<PageResponse<MessageResponse>> findAllByChannelId(
      @Parameter(description = "Channel ID") @RequestParam UUID channelId,
      @Parameter(description = "페이징 커서 정보") @RequestParam(required = false) Instant cursor,
      Pageable pageable
  );
}