package com.sprint.mission.discodeit.dto.jwt;

import com.sprint.mission.discodeit.dto.userdto.UserDto;
import lombok.Getter;


@Getter
public class JwtInformation {

  private final UserDto userDto;
  private final String accessToken;
  private final String refreshToken;

  public JwtInformation(UserDto userDto, String accessToken, String refreshToken) {
    this.userDto = userDto;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  public JwtInformation rotate(String accessToken, String refreshToken) {
    return new JwtInformation(this.userDto, accessToken, refreshToken);
  }


}
