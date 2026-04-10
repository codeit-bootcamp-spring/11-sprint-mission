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
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class),
              examples = @ExampleObject(value = AuthExamples.LOGIN_200)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Missing required fields",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "AUTH_001", description = "Missing username", value = AuthExamples.ERROR_400_AUTH_001),
                  @ExampleObject(name = "AUTH_002", description = "Missing password", value = AuthExamples.ERROR_400_AUTH_002)
              }
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Invalid credentials",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(name = "AUTH_003", value = AuthExamples.ERROR_401_AUTH_003)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User not found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(name = "USER_011", value = AuthExamples.ERROR_404_USER_011)
          )
      )
  })
  ResponseEntity<UserResponse> login(
      @RequestBody(description = "Login request body", required = true) LoginRequest loginRequest
  );
}