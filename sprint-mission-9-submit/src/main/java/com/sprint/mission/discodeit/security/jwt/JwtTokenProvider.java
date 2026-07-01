package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

  // nimbus-jose-jwt는 MACSigner/MACVerifier에 바이트 배열 형태의 키를 사용
  private final byte[] secretKey;

  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  // 생성자 초기화 - 30분, 7일 기본값
  public JwtTokenProvider(
      @Value("${discodeit.jwt.secret}") String secretKey,
      @Value("${discodeit.jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${discodeit.jwt.refresh-token-expiration}") long refreshTokenExpiration
  ) {
    this.secretKey = secretKey.getBytes();
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    UserDto userDto = userDetails.getUserDto();

    Instant now = Instant.now();

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(userDto.id().toString())
        .claim("username", userDto.username())
        .claim("role", userDto.role().name())
        .claim("type", "access")
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plusMillis(accessTokenExpiration)))
        .build();

    return sign(claims);
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    UserDto userDto = userDetails.getUserDto();

    Instant now = Instant.now();

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(userDto.id().toString())
        .claim("type", "refresh")
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plusMillis(refreshTokenExpiration)))
        .build();

    return sign(claims);
  }

  private String sign(JWTClaimsSet claims) {
    try {
      SignedJWT signedJWT = new SignedJWT(
          new JWSHeader(JWSAlgorithm.HS256),
          claims
      );

      signedJWT.sign(new MACSigner(secretKey));

      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 생성에 실패했습니다.", e);
    }
  }

  // 토근 정상 검사
  public boolean validateToken(String token) {
    try {
      parseAndValidate(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // 토큰 만료 시간 꺼내기
  public Instant getExpiration(String token) {
    JWTClaimsSet claims = parseAndValidate(token);
    return claims.getExpirationTime().toInstant();
  }

  // 토근 주인(사용자 id) 꺼내기
  public String getSubject(String token) {
    JWTClaimsSet claims = parseAndValidate(token);
    return claims.getSubject();
  }

  // Refresh Token 검증 후 새로운 Access Token 만들기
  public String reissueAccessToken(String refreshToken, DiscodeitUserDetails userDetails) {

    // Refresh Token 정상 검사
    JWTClaimsSet claims = parseAndValidate(refreshToken);

    String tokenType = (String) claims.getClaim("type");
    if (!"refresh".equals(tokenType)) {
      throw new IllegalArgumentException("Refresh Token이 아닙니다.");
    }

    // Refresh Token의 사용자 id 꺼내기
    String refreshTokenSubject = claims.getSubject();
    // 바깥에서 조회해서 넘겨준 사용자 id 꺼내기
    String userId = userDetails.getUserDto().id().toString();

    if (!refreshTokenSubject.equals(userId)) {
      throw new IllegalArgumentException("Refresh Token의 사용자 정보가 일치하지 않습니다.");
    }

    return generateAccessToken(userDetails);
  }

  // 문자열로 받은 JWT를 열어보고, 정상 토큰이면 claims를 반환
  private JWTClaimsSet parseAndValidate(String token) {
    try {
      // 문자열을 Nimbus가 이해하게 파싱
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 토근의 signature와 secretKey로 계산한 signature 일치 확인
      boolean verified = signedJWT.verify(new MACVerifier(secretKey));
      if (!verified) {
        throw new IllegalArgumentException("JWT 서명이 유효하지 않습니다.");
      }

      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

      if (claims.getExpirationTime() == null) {
        throw new IllegalArgumentException("JWT 만료 시간이 없습니다.");
      }

      Instant expiration = claims.getExpirationTime().toInstant();
      if (expiration.isBefore(Instant.now())) {
        throw new IllegalArgumentException("JWT가 만료되었습니다.");
      }

      return claims;
    } catch (ParseException | JOSEException e) {
      throw new IllegalArgumentException("JWT가 유효하지 않습니다.", e);
    }
  }
}