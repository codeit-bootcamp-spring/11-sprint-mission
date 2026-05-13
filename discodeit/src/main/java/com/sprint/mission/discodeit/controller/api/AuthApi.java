package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.AuthExamples;
import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "Authentication API")
public interface AuthApi {

  @Operation(summary = "Login")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Login successful",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error (fields contain details)",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = AuthExamples.ERROR_400))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Invalid credentials",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = AuthExamples.ERROR_401_AUTH_001))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = AuthExamples.ERROR_404_USER_003))
      )
  })
  ResponseEntity<UserResponse> login(
      @RequestBody(description = "Login request body", required = true) LoginRequest loginRequest
  );
}