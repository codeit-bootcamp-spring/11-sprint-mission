package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.dto.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtDto;
import com.sprint.mission.discodeit.security.Role;
import com.sprint.mission.discodeit.security.TokenRefreshResultDto;
import com.sprint.mission.discodeit.security.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import java.text.ParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtRegistry jwtRegistry;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UserRoleUpdateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));
    Role oldRole = user.getRole();

    user.updateRole(request.newRole());

    // 권한 수정 시 해당 사용자의 토큰 강제 만료
    jwtRegistry.invalidateJwtInformationByUserId(user.getId());

    eventPublisher.publishEvent(new RoleUpdatedEvent(user.getId(), oldRole, request.newRole()));

    log.debug("사용자 권한 수정 및 이벤트 발행 완료 - userId: {}, newRole: {}", request.userId(),
        request.newRole());

    return userMapper.toDto(user);
  }

  public TokenRefreshResultDto refreshToken(String refreshToken) {
    if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      log.warn("잘못된 형식 또는 만료된 refresh token 요청: {}", refreshToken);
      throw new RefreshTokenInvalidException();
    }

    try {
      String username = jwtTokenProvider.getSubject(refreshToken);

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (!(userDetails instanceof DiscodeitUserDetails discodeitUserDetails)) {
        log.error("UserDetails 타입이 일치하지 않습니다. username: {}", username);
        throw new RefreshTokenInvalidException();
      }

      String newAccessToken = jwtTokenProvider.generateAccessToken(discodeitUserDetails);
      String newRefreshToken = jwtTokenProvider.generateRefreshToken(discodeitUserDetails);

      JwtInformation jwtInfo = new JwtInformation(
          discodeitUserDetails.getUserDto().id(),
          newAccessToken,
          newRefreshToken,
          jwtTokenProvider.getExpiration(newRefreshToken)
      );

      jwtRegistry.rotateJwtInformation(refreshToken, jwtInfo);

      JwtDto jwtDto = new JwtDto(discodeitUserDetails.getUserDto(), newAccessToken);

      return new TokenRefreshResultDto(jwtDto, newRefreshToken);

    } catch (ParseException e) {
      log.warn("Refresh Token 파싱 중 오류 발생: {}", e.getMessage());
      throw new RefreshTokenInvalidException();
    }
  }
}
