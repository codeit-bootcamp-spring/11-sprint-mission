package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.JwtDto;

public record RefreshResult(JwtDto jwtDto, String refreshToken) {

}
