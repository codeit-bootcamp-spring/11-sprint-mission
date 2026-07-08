package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.dto.JwtDto;
import com.sprint.mission.discodeit.security.jwt.model.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import com.sprint.mission.discodeit.security.properties.JwtProperties;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  private final DiscodeitUserDetailsService userDetailsService;

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final JwtProperties jwtProperties;

  // Role Update
  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UserRoleUpdateRequest dto) {
    log.debug("[USER_ROLE_UPTATE_START] 유저 권한 수정 시작 - 권한 수정할 유저 ID={}", dto.userId());

    User user = userRepository.findById(dto.userId()).orElseThrow(
        () -> new UserNotFoundException(dto.userId())
    );

    user.updateRole(dto.newRole());

    // 만약 사용자가 로그인 상태라면 토큰 상태를 무효화시켜 강제 로그아웃
    jwtRegistry.invalidateJwtInformationByUserId(dto.userId());

    log.info("[USER_ROLE_UPDATE_SUCCESS] 유저 권한 수정 완료 - 권한 수정한 유저 ID={}", dto.userId());

    return userMapper.toDto(user);
  }

  @Override
  public JwtDto refresh(String refreshToken, HttpServletResponse response) {

    // 토큰에서 사용자 식별 정보(subject)를 추출 후 UUID로 변환
    UUID userId = UUID.fromString(jwtTokenProvider.getSubject(refreshToken));

    // UserDetails 조회
    DiscodeitUserDetails userDetails =
        (DiscodeitUserDetails) userDetailsService.loadUserById(userId);

    // JwtDto에 담기 위해 UserDto로 추출
    UserDto userDto = userDetails.getUserDto();

    // 서버에 RefreshToken이 존재하지 않을 경우 예외처리
    if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    // Refresh Token으로 Access Token 발급
    String accessToken = jwtTokenProvider.reIssueAccessToken(refreshToken);

    // Refresh Token 회전
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId.toString());

    // JwtInformation Dto를 위한 expiration 선언
    Instant expiration = jwtTokenProvider
        .getTokenExpiration(jwtProperties.getRefreshTokenExpiration())
        .toInstant();

    // JwtInformation 생성
    JwtInformation newJwtInformation = new JwtInformation(
        userId,
        accessToken,
        newRefreshToken,
        expiration
    );

    // 기존 Refresh Token을 활용하여 발급받은 Access Token, New Refresh Token를 JwtInformation으로 Registry에 저장(=교체)
    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    // Refresh Token 쿠키 교체
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, newRefreshToken);

    // JavaScript 접근 차단
    cookie.setHttpOnly(true);

    // 내 도메인 내의 모든 URI에 쿠키 적용
    cookie.setPath("/");

    // 쿠키 유효기간을 30일로 설정(setMaxAge()는 초 단위이기 때문에 60을 곱하여 초 단위로 변환)
    cookie.setMaxAge(jwtProperties.getRefreshTokenExpiration() * 60);

    // 응답에 쿠키 추가
    response.addCookie(cookie);

    return new JwtDto(userDto, accessToken);
  }
}
