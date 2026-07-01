package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

    @Operation(summary = "CSRF 토큰 발급", description = "초기 구동 시 CSRF 토큰을 발급하여 쿠키에 저장합니다.")
    ResponseEntity<Void> getCsrfToken(
            @Parameter(hidden = true) CsrfToken csrfToken
    );

    @Operation(summary = "현재 사용자 정보 조회", description = "세션 쿠키를 통해 현재 로그인된 사용자의 정보를 조회합니다.")
    ResponseEntity<UserDto> getCurrentUser(
            @Parameter(hidden = true) DiscodeitUserDetails userDetails
    );

    @Operation(summary = "사용자 권한 수정", description = "특정 사용자의 권한을 변경합니다. (ADMIN 전용)")
    ResponseEntity<UserDto> updateUserRole(
            UserRoleUpdateRequest request
    );

} 