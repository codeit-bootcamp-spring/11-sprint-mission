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

  @Transactional
  @Override
  public UserDto updateRoleInternal(RoleUpdateRequest request) {
    UUID userId = request.userId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    Role newRole = request.newRole();
    user.updateRole(newRole);

    jwtRegistry.invalidateJwtInformationByUserId(userId);

    return userMapper.toDto(user);
  }

  @Override
  public TokenPair reissueToken(String refreshToken) {
    if (refreshToken == null
        || !jwtTokenProvider.validateToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken);
    String newRefreshToken = jwtTokenProvider.reissueRefreshToken(refreshToken);

    JwtInformation newJwtInformation = new JwtInformation(
        jwtTokenProvider.getUserId(refreshToken),
        newAccessToken,
        newRefreshToken,
        jwtTokenProvider.getExpiration(newRefreshToken));
    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    return new TokenPair(newAccessToken, newRefreshToken);
  }
}
