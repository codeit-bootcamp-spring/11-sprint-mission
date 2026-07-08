package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.jwt.JwtInformation;
import java.util.UUID;

public interface JwtRegistry {

  //등록
  void registerJwtInformation(JwtInformation jwtInformation);

  //삭제
  void invalidateJwtInformationByUserId(UUID userId);


  boolean hasActiveJwtInformationByUserId(UUID userId);          // 로그인 상태 확인

  boolean hasActiveJwtInformationByAccessToken(String accessToken);   // access 확인

  boolean hasActiveJwtInformationByRefreshToken(String refreshToken); // 유효 refresh 확인

  // 재발급 시 토큰 로테이션
  JwtInformation rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation);

  // 만료된 JwtInformation 삭제
  void clearExpiredJwtInformation();
}