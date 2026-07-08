package com.sprint.mission.discodeit.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.entity.User;
import jakarta.servlet.http.Cookie;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private static final String SECRET =
      "discodeit-sprint-mission-10-jwt-secret-key";
  private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 60 * 30;
  private static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 60 * 60 * 24 * 7;

  public String generateAccessToken(User user) {
    // API 요청 인증에 사용할 짧은 수명 access token 발급함
    return generateToken(user, ACCESS_TOKEN_EXPIRATION_SECONDS);
  }

  public String generateRefreshToken(User user) {
    // access token 재발급에 사용할 긴 수명 refresh token 발급함
    return generateToken(user, REFRESH_TOKEN_EXPIRATION_SECONDS);
  }

  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 토큰 서명 검증함
      boolean verified = signedJWT.verify(new MACVerifier(SECRET.getBytes()));

      // 토큰 만료 여부 확인함
      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      boolean notExpired = expirationTime != null && expirationTime.after(new Date());

      return verified && notExpired;
    } catch (ParseException | JOSEException e) {
      return false;
    }
  }

  public String getUsername(String token) {
    try {
      // subject에 username 저장해두었으므로 꺼내서 사용함
      return SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getSubject();
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid JWT token.", e);
    }
  }

  public UUID getUserId(String token) {
    try {
      // userId claim을 UUID로 변환함
      String userId = SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getStringClaim("userId");
      return UUID.fromString(userId);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid JWT token.", e);
    }
  }

  public Instant getExpirationTime(String token) {
    try {
      // JwtRegistry에서 만료 토큰 정리할 때 사용함
      Date expirationTime = SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getExpirationTime();
      return expirationTime.toInstant();
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid JWT token.", e);
    }
  }

  public Cookie createRefreshTokenCookie(String refreshToken) {
    // refresh token은 브라우저 쿠키에 저장함
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge((int) REFRESH_TOKEN_EXPIRATION_SECONDS);
    return cookie;
  }

  public Cookie createExpiredRefreshTokenCookie() {
    // 로그아웃 시 refresh token 쿠키 삭제용 쿠키 생성함
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    return cookie;
  }

  private String generateToken(User user, long expirationSeconds) {
    try {
      Instant now = Instant.now();
      Instant expirationTime = now.plusSeconds(expirationSeconds);

      JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
          .subject(user.getUsername())
          .claim("userId", user.getId().toString())
          .claim("role", user.getRole().name())
          .issueTime(Date.from(now))
          .expirationTime(Date.from(expirationTime))
          .build();

      SignedJWT signedJWT = new SignedJWT(
          new JWSHeader(JWSAlgorithm.HS256),
          claimsSet
      );

      signedJWT.sign(new MACSigner(SECRET.getBytes()));
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to generate JWT token.", e);
    }
  }
}