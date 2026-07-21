package com.sprint.mission.discodeit.security.jwt;

import java.util.UUID;

public interface JwtRegistry {

  // 1. 로그인 성공 시 발급 내역(jwtInformation)을 장부에 등록
  void registerJwtInformation(JwtInformation jwtInformation);

  // 2. 유저의 모든 토큰 무효화
  void invalidateJwtInformationByUserId(UUID userId);

  // 2-1. 접속한 기기만 로그아웃 처리하기 위해 RefreshToken 기반 토큰 무효화
  void invalidateJwtInformationByRefreshToken(String refreshToken);

  // 3. 유저가 현재 로그인 상태인지(장부에 남아있는지) 확인
  boolean hasActiveJwtInformationByUserId(UUID userId);

  // 4. 필터에서 넘어온 엑세스 토큰과 장부를 대조하여 유효한지 검증
  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  // 5. 토큰 재발급 API에서 넘어온 리프레시 토큰과 장부를 대조하여 유효한지 검증
  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  // 6. 리프레시 토큰 로테이션(RTR) 처리 (기존 토큰 지우고 새 토큰 쌍 등록)
  JwtInformation rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation);

  // 7. 스케줄러를 활용해 만료 시간이 지난 토큰(or 칸) 삭제
  void clearExpiredJwtInformation();
}
