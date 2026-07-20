package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.JwtRefreshResult;

public interface JwtService {

  JwtRefreshResult refreshJwtSession(String refreshToken);
}