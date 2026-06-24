package com.sprint.mission.discodeit.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
  }
