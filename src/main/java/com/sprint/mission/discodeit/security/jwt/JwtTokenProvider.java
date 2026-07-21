package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.exception.auth.JwtGenerationFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private final long accessTokenExpiration;
  @Getter
  private final long refreshTokenExpiration;

  private final JWSSigner jwsSigner;
  private final JWSVerifier jwsVerifier;

  public JwtTokenProvider(@Value("${discodeit.jwt.secret}") String secretKey,
      @Value("${discodeit.jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${discodeit.jwt.refresh-token-expiration}") long refreshTokenExpiration)
      throws JOSEException {

    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;

    byte[] secretBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    this.jwsSigner = new MACSigner(secretBytes);
    this.jwsVerifier = new MACVerifier(secretBytes);
  }


  // Access Token 발급
  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    return generateToken(userDetails, accessTokenExpiration, "access", true);
  }

  // Refresh Token 발급
  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    return generateToken(userDetails, refreshTokenExpiration, "refresh", false);
  }

  // 공통 토큰 생성 로직
  private String generateToken(DiscodeitUserDetails userDetails, long expiration, String tokenType,
      boolean includeRoles) {
    try {
      String tokenId = UUID.randomUUID().toString();
      UserDto userDto = userDetails.getUserDto();
      Date now = new Date();
      Date expiryDate = new Date(now.getTime() + expiration);

      JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
          .subject(userDto.username())
          .jwtID(tokenId)
          .claim("userId", userDto.id().toString())
          .claim("type", tokenType)
          .issueTime(now)
          .expirationTime(expiryDate);

      if (includeRoles) {
        claimsBuilder.claim("roles", "ROLE_" + userDto.role().name());
      }

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsBuilder.build());
      signedJWT.sign(this.jwsSigner);

      log.debug("[{}] 토큰 발급 완료 - Username: {}", tokenType, userDto.username());
      return signedJWT.serialize();

    } catch (JOSEException e) {
      log.error("JWT 서명 중 서버 내부 인프라 오류 발생: {}", e.getMessage());
      throw new JwtGenerationFailedException();
    }
  }

  // Access Token 검증
  public boolean validateAccessToken(String token) {
    return validateToken(token, "access");
  }

  // Refresh Token 검증
  public boolean validateRefreshToken(String token) {
    return validateToken(token, "refresh");
  }

  //  공통 토큰 검증 로직
  private boolean validateToken(String token, String expectedType) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      if (!signedJWT.verify(this.jwsVerifier)) {
        log.warn("JWT 서명 검증 실패 - 타입: {}", expectedType);
        return false;
      }

      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      if (!expectedType.equals(tokenType)) {
        log.warn("JWT 타입 불일치 (기대값: {}, 실제값: {})", expectedType, tokenType);
        return false;
      }

      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expirationTime == null || expirationTime.before(new Date())) {
        log.warn("JWT 토큰 만료 - 타입: {}", expectedType);
        return false;
      }

      return true;
    } catch (JOSEException | ParseException e) {
      log.warn("JWT 파싱/서명 검증 에러: {}", e.getMessage());
      return false;
    }
  }

  // 점보 추출 유틸리티 (필터 등에서 사용)

  public String getSubject(String token) throws ParseException {
    return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
  }

  public String getTokenType(String token) throws ParseException {
    return SignedJWT.parse(token).getJWTClaimsSet().getStringClaim("type");
  }

  public UUID getUserId(String token) throws ParseException {
    String userIdStr = (String) SignedJWT.parse(token).getJWTClaimsSet().getClaim("userId");
    return UUID.fromString(userIdStr);
  }

  public Instant getExpiration(String token) throws ParseException {
    return SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime().toInstant();
  }

  public String getRole(String token) throws ParseException {
    return SignedJWT.parse(token).getJWTClaimsSet().getStringClaim("roles");
  }
}
