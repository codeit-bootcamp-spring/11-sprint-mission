package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.dto.JwtDto;
import com.sprint.mission.discodeit.security.jwt.dto.RefreshTokenResult;
import com.sprint.mission.discodeit.security.jwt.model.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.properties.JwtProperties;
import com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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

  private final ApplicationEventPublisher eventPublisher;

  // Role Update
  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UserRoleUpdateRequest dto) {
    log.debug("[USER_ROLE_UPTATE_START] 유저 권한 수정 시작 - 권한 수정할 유저 ID={}", dto.userId());

    User user = userRepository.findById(dto.userId()).orElseThrow(
        () -> new UserNotFoundException(dto.userId())
    );

    Role beforeRole = user.getRole();

    user.updateRole(dto.newRole());

    eventPublisher.publishEvent(
        new RoleUpdatedEvent(
            user.getId(),
            beforeRole,
            user.getRole()
        )
    );

    // 만약 사용자가 로그인 상태라면 토큰 상태를 무효화시켜 강제 로그아웃
    jwtRegistry.invalidateJwtInformationByUserId(dto.userId());

    log.info("[USER_ROLE_UPDATE_SUCCESS] 유저 권한 수정 완료 - 권한 수정한 유저 ID={}", dto.userId());

    return userMapper.toDto(user);
  }

  @Override
  public RefreshTokenResult refresh(String refreshToken) {

    // 서버에 RefreshToken이 존재하지 않을 경우 예외처리
    if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    // 토큰에서 사용자 식별 정보(subject)를 추출 후 UUID로 변환
    UUID userId = UUID.fromString(jwtTokenProvider.getSubject(refreshToken));

    // UserDetails 조회
    DiscodeitUserDetails userDetails =
        (DiscodeitUserDetails) userDetailsService.loadUserById(userId);

    // JwtDto에 담기 위해 UserDto로 추출
    UserDto userDto = userDetails.getUserDto();

    // Refresh Token으로 Access Token 발급
    String accessToken = jwtTokenProvider.reIssueAccessToken(refreshToken);

    // Refresh Token 회전
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId.toString());

    // JwtInformation Dto를 위한 expiration 선언
    Instant expiration = jwtTokenProvider
        .getTokenExpiration(jwtProperties.getRefreshTokenExpiration());

    // JwtInformation 생성
    JwtInformation newJwtInformation = new JwtInformation(
        userId,
        accessToken,
        newRefreshToken,
        expiration
    );

    // 기존 Refresh Token을 활용하여 발급받은 Access Token, New Refresh Token를 JwtInformation으로 Registry에 저장(=교체)
    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    JwtDto jwtDto = new JwtDto(userDto, accessToken);

    return new RefreshTokenResult(jwtDto, newRefreshToken);
  }
}
