package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private static final String TOKEN_TYPE_ACCESS = "access";
  private static final String TOKEN_TYPE_REFRESH = "refresh";

  private final byte[] accessSecretKey;
  private final byte[] refreshSecretKey;

  private final int accessTokenExpirationMs;
  private final int refreshTokenExpirationMs;

  public JwtTokenProvider(
      @Value("${discodeit.jwt.access-token.secret}") String accessSecret,
      @Value("${discodeit.jwt.access-token.expiration}") int accessTokenExpirationMs,
      @Value("${discodeit.jwt.refresh-token.secret}") String refreshSecret,
      @Value("${discodeit.jwt.refresh-token.expiration}") int refreshTokenExpirationMs) {
    this.accessSecretKey = accessSecret.getBytes(StandardCharsets.UTF_8);
    this.refreshSecretKey = refreshSecret.getBytes(StandardCharsets.UTF_8);
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
  }

  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    return generateToken(userDetails, accessSecretKey, accessTokenExpirationMs, TOKEN_TYPE_ACCESS);
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    return generateToken(userDetails, refreshSecretKey, refreshTokenExpirationMs,
        TOKEN_TYPE_REFRESH);
  }

  private String generateToken(DiscodeitUserDetails userDetails, byte[] secretKey,
      long expirationMs, String type) {
    try {
      UserDto.Response user = userDetails.getUserDto();
      Date now = new Date();
      Date expiryDate = new Date(now.getTime() + expirationMs);

      JWTClaimsSet claims = new JWTClaimsSet.Builder()
          .subject(user.username())
          .claim("userId", user.id().toString())
          .claim("role", user.role().name())
          .claim("type", type)
          .issueTime(now)
          .expirationTime(expiryDate)
          .build();

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
      signedJWT.sign(new MACSigner(secretKey));
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 생성에 실패했습니다.", e);
    }
  }

  public boolean validateAccessToken(String token) {
    return validate(token, accessSecretKey, TOKEN_TYPE_ACCESS);
  }

  public boolean validateRefreshToken(String token) {
    return validate(token, refreshSecretKey, TOKEN_TYPE_REFRESH);
  }

  private boolean validate(String token, byte[] secretKey, String expectedType) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      if (!signedJWT.verify(new MACVerifier(secretKey))) {
        log.debug("JWT 서명 검증 실패: {}", expectedType);
        return false;
      }

      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

      String tokenType = claims.getStringClaim("type");
      if (!expectedType.equals(tokenType)) {
        log.debug("JWT 타입 불일치: 예상:{}, 실제:{}", expectedType, tokenType);
        return false;
      }

      Date expiration = claims.getExpirationTime();
      if (expiration == null || expiration.before(new Date())) {
        log.debug("JWT 만료: {}", expectedType);
        return false;
      }
      return true;
    } catch (Exception e) {
      log.debug("JWT 검증 실패 {} 예외 발생: {}", expectedType, e.getMessage());
      return false;
    }
  }

  public Instant getExpiration(String token) {
    return parse(token).getExpirationTime().toInstant();
  }

  public String getSubject(String token) {
    return parse(token).getSubject();
  }


  public UUID getUserId(String token) {
    String userIdStr = (String) parse(token).getClaim("userId");
    if (userIdStr == null) {
      throw new IllegalArgumentException("JWT에 userId 정보가 없습니다.");
    }
    return UUID.fromString(userIdStr);
  }

  public Role getRole(String token) {
    String roleStr = (String) parse(token).getClaim("role");

    if (roleStr == null) {
      throw new IllegalArgumentException("JWT에 role 정보가 없습니다.");
    }
    return Role.valueOf(roleStr);
  }

  private JWTClaimsSet parse(String token) {
    try {
      return SignedJWT.parse(token).getJWTClaimsSet();
    } catch (Exception e) {
      log.debug("JWT 파싱 실패: {}", e.getMessage());
      throw new IllegalArgumentException("유효하지 않은 JWT입니다.", e);
    }
  }

  public String reissueAccessToken(String refreshToken) {
    if (!validateRefreshToken(refreshToken)) {
      throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
    }
    JWTClaimsSet claims = parse(refreshToken);
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

    try {
      JWTClaimsSet newClaims = new JWTClaimsSet.Builder()
          .subject(claims.getSubject())
          .claim("userId", claims.getClaim("userId"))
          .claim("role", claims.getClaim("role"))
          .claim("type", TOKEN_TYPE_ACCESS)
          .issueTime(now)
          .expirationTime(expiry)
          .build();

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), newClaims);
      signedJWT.sign(new MACSigner(accessSecretKey));
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 재발급에 실패했습니다.", e);
    }
  }
}