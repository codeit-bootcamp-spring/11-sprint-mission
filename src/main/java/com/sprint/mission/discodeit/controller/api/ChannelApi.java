package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.ChannelExamples;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Channel", description = "Channel API")
@RequestMapping("/api/channels")
public interface ChannelApi {

  @Operation(summary = "Create public channel")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Public channel created successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ChannelResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error (fields contain details)",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ChannelExamples.ERROR_400_VALIDATION))
      ),
      @ApiResponse(
          responseCode = "409",
          description = "Duplicate channel name",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ChannelExamples.ERROR_409_CHANNEL_001))
      )
  })
  ResponseEntity<ChannelResponse> create(
      @RequestBody(description = "Public channel creation request", required = true) PublicChannelCreateRequest publicChannelCreateRequest
  );

  @Operation(summary = "Create private channel")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Private channel created successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ChannelResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid participants (fields contain details)",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "COMMON_001", description = "Missing participants", value = ChannelExamples.ERROR_400_VALIDATION),
                  @ExampleObject(name = "CHANNEL_002", description = "No valid participants", value = ChannelExamples.ERROR_400_CHANNEL_002)
              })
      )
  })
  ResponseEntity<ChannelResponse> create(
      @RequestBody(description = "Private channel creation request", required = true) PrivateChannelCreateRequest privateChannelCreateRequest
  );

  @Operation(summary = "Update channel")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Channel updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ChannelResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Channel not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ChannelExamples.ERROR_404_CHANNEL_003))
      ),
      @ApiResponse(
          responseCode = "409",
          description = "Duplicate channel name",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ChannelExamples.ERROR_409_CHANNEL_001))
      ),
      @ApiResponse(
          responseCode = "422",
          description = "Private channel cannot be updated",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ChannelExamples.ERROR_422_CHANNEL_004))
      )
  })
  ResponseEntity<ChannelResponse> update(
      @Parameter(description = "Channel ID") @PathVariable UUID channelId,
      @RequestBody PublicChannelUpdateRequest publicChannelUpdateRequest
  );

  @Operation(summary = "Delete channel")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Channel deleted successfully"),
      @ApiResponse(
          responseCode = "404",
          description = "Channel not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = ChannelExamples.ERROR_404_CHANNEL_003))
      )
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "Channel ID") @PathVariable UUID channelId
  );

  @Operation(summary = "Find all channels by user")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Channels retrieved successfully",
          content = @Content(mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = ChannelResponse.class)))
      )
  })
  ResponseEntity<List<ChannelResponse>> findAllByUserId(
      @Parameter(description = "User ID") @RequestParam UUID userId
  );
}