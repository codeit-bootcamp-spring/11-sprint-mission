package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

    @Operation(summary =  "CSRF 토큰 발급", description = "SPA에서 CSRF 토큰을 쿠키(XSRF-TOKEN)로 발급받기 위한 API")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204", description = "CSRF 토큰이 쿠키로 발급됨"
    )
})
ResponseEntity<Void> csrfToken(
        @Parameter(hidden = true) CsrfToken csrfToken
);

    @Operation(summary = "내 정보 조회", description = "인증된 사용자 본인의 정보를 UserDto로 조회")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            )
    })
    ResponseEntity<UserDto> me(
            @Parameter(hidden = true)DiscodeitUserDetails principal
            );
  }
