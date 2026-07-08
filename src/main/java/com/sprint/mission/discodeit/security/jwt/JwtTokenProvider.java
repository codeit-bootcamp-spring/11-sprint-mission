package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.ExpiredJwtTokenException;
import com.sprint.mission.discodeit.exception.jwt.InvalidJwtTokenException;
import com.sprint.mission.discodeit.exception.jwt.JwtException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private static final String CLAIM_USERNAME = "username";
  private static final String CLAIM_EMAIL = "email";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_TOKEN_TYPE = "tokenType";

  private final MACSigner signer;
  private final MACVerifier verifier;
  private final long accessTokenExpirationSeconds;
  private final long refreshTokenExpirationSeconds;

  public JwtTokenProvider(
      @Value("${discodeit.jwt.secret}") String secret,
      @Value("${discodeit.jwt.access-token-expiration}") long accessTokenExpirationSeconds,
      @Value("${discodeit.jwt.refresh-token-expiration}") long refreshTokenExpirationSeconds
  ) {
    try {
      byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
      this.signer = new MACSigner(secretBytes);
      this.verifier = new MACVerifier(secretBytes);
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 시크릿 키 초기화에 실패했습니다. 최소 32바이트 이상이어야 합니다.", e);
    }
    this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
  }

  public String generateAccessToken(UserDto userDto) {
    return generateToken(userDto.id(), userDto.username(), userDto.email(), userDto.role(),
        accessTokenExpirationSeconds, TokenType.ACCESS);
  }

  public String generateRefreshToken(UserDto userDto) {
    return generateToken(userDto.id(), userDto.username(), userDto.email(), userDto.role(),
        refreshTokenExpirationSeconds, TokenType.REFRESH);
  }

  public boolean validateToken(String token) {
    try {
      parseAndValidate(token);
      return true;
    } catch (JwtException e) {
      log.debug("JWT 유효성 검사 실패: {}", e.getMessage());
      return false;
    }
  }

  public JWTClaimsSet getClaims(String token) {
    return parseAndValidate(token);
  }

  public UUID getUserId(String token) {
    return getUserId(getClaims(token));
  }

  public String getUsername(String token) {
    return getStringClaim(getClaims(token), CLAIM_USERNAME);
  }

  public String getEmail(String token) {
    return getStringClaim(getClaims(token), CLAIM_EMAIL);
  }

  public Role getRole(String token) {
    return Role.valueOf(getStringClaim(getClaims(token), CLAIM_ROLE));
  }

  public TokenType getTokenType(String token) {
    return TokenType.valueOf(getStringClaim(getClaims(token), CLAIM_TOKEN_TYPE));
  }

  public long getAccessTokenExpirationSeconds() {
    return accessTokenExpirationSeconds;
  }

  public long getRefreshTokenExpirationSeconds() {
    return refreshTokenExpirationSeconds;
  }

  public ResponseCookie buildRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofSeconds(refreshTokenExpirationSeconds))
        .build();
  }

  public ResponseCookie expireRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ZERO)
        .build();
  }

  private String generateToken(UUID userId, String username, String email, Role role,
      long expirationSeconds, TokenType tokenType) {
    Instant now = Instant.now();
    Instant expiration = now.plusSeconds(expirationSeconds);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(userId.toString())
        .claim(CLAIM_USERNAME, username)
        .claim(CLAIM_EMAIL, email)
        .claim(CLAIM_ROLE, role.name())
        .claim(CLAIM_TOKEN_TYPE, tokenType.name())
        .issueTime(Date.from(now))
        .expirationTime(Date.from(expiration))
        .build();

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 서명에 실패했습니다.", e);
    }
    return signedJWT.serialize();
  }

  private JWTClaimsSet parseAndValidate(String token) {
    SignedJWT signedJWT;
    try {
      signedJWT = SignedJWT.parse(token);
      if (!signedJWT.verify(verifier)) {
        throw new InvalidJwtTokenException();
      }
    } catch (ParseException | JOSEException e) {
      throw new InvalidJwtTokenException(e);
    }

    JWTClaimsSet claims;
    try {
      claims = signedJWT.getJWTClaimsSet();
    } catch (ParseException e) {
      throw new InvalidJwtTokenException(e);
    }

    Date expiration = claims.getExpirationTime();
    if (expiration == null || expiration.before(new Date())) {
      throw new ExpiredJwtTokenException();
    }

    return claims;
  }

  private UUID getUserId(JWTClaimsSet claims) {
    try {
      return UUID.fromString(claims.getSubject());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidJwtTokenException(e);
    }
  }

  private String getStringClaim(JWTClaimsSet claims, String name) {
    try {
      return claims.getStringClaim(name);
    } catch (ParseException e) {
      throw new InvalidJwtTokenException(e);
    }
  }
}
