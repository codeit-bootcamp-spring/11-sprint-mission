package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.JwtRefreshResult;

public interface JwtAuthService {

  JwtRefreshResult refresh(String refreshToken);
}