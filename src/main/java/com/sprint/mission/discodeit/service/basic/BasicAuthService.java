package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.TokenPair;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtRegistry jwtRegistry;
  private final JwtTokenProvider jwtTokenProvider;

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Override
  public UserDto updateRole(RoleUpdateRequest request) {
    return updateRoleInternal(request);
  }

  // 관리자 계정 부트스트랩 전용 권한 부여 경로. @PreAuthorize를 우회하므로 인터페이스에 노출하지 않는다.
  @Transactional
  public UserDto updateRoleInternal(RoleUpdateRequest request) {
    UUID userId = request.userId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    Role newRole = request.newRole();
    user.updateRole(newRole);

    jwtRegistry.invalidateJwtInformationByUserId(userId);

    return userMapper.toDto(user);
  }

  @Transactional(readOnly = true)
  @Override
  public TokenPair reissueToken(String refreshToken) {
    if (refreshToken == null
        || !jwtTokenProvider.validateToken(refreshToken)
        || !jwtTokenProvider.isRefreshToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    // 재발급 시 DB에서 최신 사용자 정보를 조회해 권한 변경을 반영한다.
    UUID userId = jwtTokenProvider.getUserId(refreshToken);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
    DiscodeitUserDetails userDetails =
        new DiscodeitUserDetails(userMapper.toDto(user), user.getPassword());

    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    JwtInformation newJwtInformation = new JwtInformation(
        userId,
        newRefreshToken,
        jwtTokenProvider.getExpiration(newRefreshToken));
    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    return new TokenPair(newAccessToken, newRefreshToken);
  }
}
