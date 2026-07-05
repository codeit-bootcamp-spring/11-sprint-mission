package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.jwt.InvalidJwtTokenException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.TokenType;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.RefreshResult;
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

    user.updateRefreshToken(null);

    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public void saveRefreshToken(UUID userId, String refreshToken) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
    user.updateRefreshToken(refreshToken);
  }

  @Transactional
  @Override
  public RefreshResult refresh(String refreshToken) {
    if (refreshToken == null) {
      throw new InvalidJwtTokenException();
    }

    if (jwtTokenProvider.getTokenType(refreshToken) != TokenType.REFRESH) {
      throw new InvalidJwtTokenException();
    }

    UUID userId = jwtTokenProvider.getUserId(refreshToken);
    User user = userRepository.findById(userId)
        .orElseThrow(InvalidJwtTokenException::new);

    if (!refreshToken.equals(user.getRefreshToken())) {
      user.updateRefreshToken(null);
      throw new InvalidJwtTokenException();
    }

    UserDto userDto = userMapper.toDto(user);
    String newAccessToken = jwtTokenProvider.generateAccessToken(userDto);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDto);
    user.updateRefreshToken(newRefreshToken);

    return new RefreshResult(new JwtDto(userDto, newAccessToken), newRefreshToken);
  }
}
