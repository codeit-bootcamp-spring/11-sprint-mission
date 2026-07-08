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
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private final String secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;
  private final String base64EncodedSecretKey;

  public JwtTokenProvider(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

    this.secretKey = secretKey;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;

    this.base64EncodedSecretKey = Base64.getEncoder()
        .encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  // access 토큰 생성
  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    return createToken(userDetails.getUsername(), accessTokenExpiration);
  }

  // refresh 토큰 생성
  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    return createToken(userDetails.getUsername(), refreshTokenExpiration);
  }

  // 토큰 생성
  private String createToken(String subject, long expirationTime) {
    try {
      byte[] keyBytes = Base64.getDecoder().decode(base64EncodedSecretKey);
      JWSSigner signer = new MACSigner(keyBytes);

      JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
          .subject(subject)
          .issueTime(new Date())
          .expirationTime(new Date(System.currentTimeMillis() + expirationTime))
          .build();

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
      signedJWT.sign(signer);
      return signedJWT.serialize();

    } catch (JOSEException e) {
      log.error("JWT 생성 중 에러 발생", e);
      throw new RuntimeException("JWT 토큰 생성 실패", e);
    }
  }

  // 유효성 검사
  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      byte[] keyBytes = Base64.getDecoder().decode(base64EncodedSecretKey);
      JWSVerifier verifier = new MACVerifier(keyBytes);

      if (!signedJWT.verify(verifier)) {
        log.warn("유효하지 않은 JWT 서명입니다.");
        return false;
      }

      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expirationTime == null || expirationTime.before(new Date())) {
        log.warn("만료된 JWT 토큰입니다.");
        return false;
      }

      return true;
    } catch (ParseException | JOSEException e) {
      log.warn("지원되지 않거나 잘못된 형식의 JWT 토큰입니다.", e);
      return false;
    }
  }

  // 토큰 만료 시간 추출
  public Instant getExpiration(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      return expirationTime != null ? expirationTime.toInstant() : null;
    } catch (ParseException e) {
      log.error("JWT 토큰 파싱 에러 (만료 시간 추출 실패)", e);
      return null;
    }
  }

  // 토큰 Subject 추출
  public String getSubject(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      log.error("JWT 토큰 파싱 에러 (Subject 추출 실패)", e);
      return null;
    }
  }

  // 토큰 갱신
  public String reissueAccessToken(String refreshToken) {
    if (!validateToken(refreshToken)) {
      throw new IllegalStateException("유효하지 않은 Refresh Token 입니다.");
    }
    String subject = getSubject(refreshToken);
    return createToken(subject, accessTokenExpiration);
  }
}