package com.sprint.mission.discodeit.security.jwt.registry;

import com.sprint.mission.discodeit.security.jwt.model.JwtInformation;
import java.util.UUID;

public interface JwtRegistry {

  // 로그인 성공 시 JwtInformation 등록
  void registerJwtInformation(JwtInformation jwtInformation);

  // 특정 사용자의 모든 JwtInformation을 무효화(삭제)
  void invalidateJwtInformationByUserId(UUID userId);

  // 특정 사용자ID가 들어있는 JwtInformation이 Registry에 존재하는지 확인
  boolean hasActiveJwtInformationByUserId(UUID userId);

  // 특정 Access Token이 들어있는 JwtInformation이 Registry에 존재하는지 확인
  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  // 특정 Refresh Token이 들어있는 JwtInformation이 Registry에 존재하는지 확인
  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  // JwtInformation 교체(수정X, 삭제 → 생성으로 갱신)
  JwtInformation rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation);

  // 만료된 JwtInformation을 스케줄러를 통해 정리
  void clearExpiredJwtInformation();

  // 특정 Refresh Token을 무효화
  void invalidateJwtInformationByRefreshToken(String refreshToken);

}
