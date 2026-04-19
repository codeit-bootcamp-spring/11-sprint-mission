package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.ReadStatusExamples;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "ReadStatus", description = "Read Status API")
public interface ReadStatusApi {

  @Operation(summary = "Create read status")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Read status created successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ReadStatusResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error (fields contain details)",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ReadStatusExamples.ERROR_400))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User or channel not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "USER_003", description = "User not found", value = ReadStatusExamples.ERROR_404_USER_003),
                  @ExampleObject(name = "CHANNEL_003", description = "Channel not found", value = ReadStatusExamples.ERROR_404_CHANNEL_003)
              })
      ),
      @ApiResponse(
          responseCode = "409",
          description = "Read status already exists",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ReadStatusExamples.ERROR_409_READ_STATUS_001))
      )
  })
  ResponseEntity<ReadStatusResponse> create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Read status creation request", required = true)
      @RequestBody ReadStatusCreateRequest readStatusCreateRequest
  );

  @Operation(summary = "Update read status")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Read status updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ReadStatusResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error (fields contain details)",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ReadStatusExamples.ERROR_400))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Read status not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ReadStatusExamples.ERROR_404_READ_STATUS_002))
      )
  })
  ResponseEntity<ReadStatusResponse> update(
      @Parameter(description = "Read status ID") @PathVariable UUID readStatusId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Read status update request", required = true)
      @RequestBody ReadStatusUpdateRequest readStatusUpdateRequest
  );

  @Operation(summary = "Find all read statuses by user")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Read statuses retrieved successfully",
          content = @Content(mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = ReadStatusResponse.class)))
      )
  })
  ResponseEntity<List<ReadStatusResponse>> findAllByUserId(
      @Parameter(description = "User ID") @RequestParam UUID userId
  );
}