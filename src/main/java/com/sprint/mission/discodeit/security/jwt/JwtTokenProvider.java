package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_ROLE = "role";

  private final SecretKey key;
  private final JwtProperties jwtProperties;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    this.jwtProperties = jwtProperties;
  }

  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    return generateToken(userDetails, jwtProperties.accessTokenExpiration());
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    return generateToken(userDetails, jwtProperties.refreshTokenExpiration());
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

  public Instant getExpiration(String token) {
    return parseClaims(token).getExpiration().toInstant();
  }

  public long getAccessTokenExpiration() {
    return jwtProperties.accessTokenExpiration();
  }

  public long getRefreshTokenExpiration() {
    return jwtProperties.refreshTokenExpiration();
  }

  // 리프레시 토큰으로 새 액세스 토큰 재발급
  public String reissueAccessToken(String refreshToken) {
    return reissueToken(refreshToken, jwtProperties.accessTokenExpiration());
  }

  // 리프레시 토큰 로테이션: 기존 클레임으로 새 리프레시 토큰 발급
  public String reissueRefreshToken(String refreshToken) {
    return reissueToken(refreshToken, jwtProperties.refreshTokenExpiration());
  }

  private String reissueToken(String token, long expiration) {
    Claims claims = parseClaims(token);
    return generateToken(
        claims.getSubject(),
        claims.get(CLAIM_USER_ID, String.class),
        claims.get(CLAIM_ROLE, String.class),
        expiration);
  }

  // 토큰 생성
  private String generateToken(DiscodeitUserDetails userDetails, long expiration) {
    return generateToken(
        userDetails.getUsername(),
        userDetails.getUserDto().id().toString(),
        userDetails.getUserDto().role().name(),
        expiration);
  }

  private String generateToken(String subject, String userId, String role, long expiration) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(subject)
        .claim(CLAIM_USER_ID, userId)
        .claim(CLAIM_ROLE, role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expiration)))
        .signWith(key)
        .compact();
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