package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_TYPE = "type";

  private final SecretKey key;
  private final JwtProperties jwtProperties;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    this.jwtProperties = jwtProperties;
  }

  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    return generateToken(userDetails, TokenType.ACCESS, jwtProperties.accessTokenExpiration());
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    return generateToken(userDetails, TokenType.REFRESH, jwtProperties.refreshTokenExpiration());
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
      return false;
    }
  }

  public String getUsername(String token) {
    return parseClaims(token).getSubject();
  }

  public UUID getUserId(String token) {
    return UUID.fromString(parseClaims(token).get(CLAIM_USER_ID, String.class));
  }

  public Role getRole(String token) {
    return Role.valueOf(parseClaims(token).get(CLAIM_ROLE, String.class));
  }

  public boolean isAccessToken(String token) {
    return getType(token) == TokenType.ACCESS;
  }

  public boolean isRefreshToken(String token) {
    return getType(token) == TokenType.REFRESH;
  }

  public Instant getExpiration(String token) {
    return parseClaims(token).getExpiration().toInstant();
  }

  public long getAccessTokenExpiration() {
    return jwtProperties.accessTokenExpiration();
  }

  // 리프레시 토큰 쿠키 생성을 한 곳에서 관리한다. (Secure 등 보안 속성 일관성 보장)
  public ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return baseRefreshTokenCookie(refreshToken)
        .maxAge(Duration.ofMillis(jwtProperties.refreshTokenExpiration()))
        .build();
  }

  public ResponseCookie expireRefreshTokenCookie() {
    return baseRefreshTokenCookie("")
        .maxAge(0)
        .build();
  }

  private ResponseCookie.ResponseCookieBuilder baseRefreshTokenCookie(String value) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
        .httpOnly(true)
        .secure(jwtProperties.cookieSecure())
        .path("/")
        .sameSite("Strict");
  }

  // 토큰 생성
  private String generateToken(DiscodeitUserDetails userDetails, TokenType type, long expiration) {
    return generateToken(
        userDetails.getUsername(),
        userDetails.getUserDto().id().toString(),
        userDetails.getUserDto().role().name(),
        type,
        expiration);
  }

  private String generateToken(String subject, String userId, String role, TokenType type,
      long expiration) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(subject)
        .claim(CLAIM_USER_ID, userId)
        .claim(CLAIM_ROLE, role)
        .claim(CLAIM_TYPE, type.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expiration)))
        .signWith(key)
        .compact();
  }

  private TokenType getType(String token) {
    String type = parseClaims(token).get(CLAIM_TYPE, String.class);
    return type == null ? null : TokenType.valueOf(type);
  }

  // 토큰 파싱
  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}