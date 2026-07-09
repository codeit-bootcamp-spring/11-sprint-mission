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
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import jakarta.annotation.PostConstruct;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private static final String CLAIM_USERNAME = "username";
  private static final String CLAIM_TYPE = "type";
  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_REFRESH = "refresh";

  @Value("${discodeit.security.jwt.secret}")
  private String secretKey;

  @Value("${discodeit.security.jwt.access-token-validity}")
  private long accessTokenValidity;

  @Value("${discodeit.security.jwt.refresh-token-validity}")
  private long refreshTokenValidity;

  @Value("${discodeit.security.jwt.cookie-secure:true}")
  private boolean cookieSecure;

  private JWSSigner signer;
  private JWSVerifier verifier;

  public record TokenPair(String accessToken, String refreshToken) {

  }

  @PostConstruct
  public void init() {
    byte[] keyBytes = secretKey.getBytes();
    if (keyBytes.length < 32) {
      throw new IllegalStateException("JWT secret key는 최소 256비트(32바이트) 이상이어야 합니다. 현재길이: "
          + keyBytes.length + "바이트");
    }
    try {
      this.signer = new MACSigner(secretKey.getBytes());
      this.verifier = new MACVerifier(secretKey.getBytes());
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 서명/검증기 초기화 실패", e);
    }
  }

  public String createAccessToken(UUID userId, String username) {
    return createToken(userId, username, accessTokenValidity, TYPE_ACCESS);
  }

  public String createRefreshToken(UUID userId, String username) {
    return createToken(userId, username, refreshTokenValidity, TYPE_REFRESH);
  }

  private String createToken(UUID userId, String username, long validitySeconds, String type) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + validitySeconds * 1000);

    JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
        .subject(userId.toString())
        .issueTime(now)
        .expirationTime(expiry)
        .claim(CLAIM_TYPE, type);

    if (username != null) {
      claimsBuilder.claim(CLAIM_USERNAME, username);
    }

    JWTClaimsSet claims = claimsBuilder.build();

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS256),
        claims
    );

    try {
      signedJWT.sign(signer);
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("토큰 생성 실패", e);
    }
  }

  // 정보 추출
  public UUID getUserId(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      String subject = signedJWT.getJWTClaimsSet().getSubject();
      return UUID.fromString(subject);
    } catch (ParseException e) {
      throw new IllegalArgumentException("유효하지 않은 토큰입니다", e);
    }
  }

  public String getUsername(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet().getStringClaim(CLAIM_USERNAME);
    } catch (ParseException e) {
      throw new IllegalArgumentException("유효하지 않은 토큰입니다.", e);
    }
  }

  public Instant getExpiration(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
      return expiration != null ? expiration.toInstant() : null;
    } catch (ParseException e) {
      throw new IllegalArgumentException("유효하지 않은 토큰입니다.", e);
    }
  }

  // 유효성 검사
  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      if (!signedJWT.verify(verifier)) {
        return false;
      }
      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
      return expiration != null && expiration.after(new Date());
    } catch (JOSEException | ParseException e) {
      return false;
    }
  }

  public boolean isAccessToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      String type = signedJWT.getJWTClaimsSet().getStringClaim(CLAIM_TYPE);
      return TYPE_ACCESS.equals(type);
    } catch (ParseException e) {
      return false;
    }
  }

  public boolean isRefreshToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      String type = signedJWT.getJWTClaimsSet().getStringClaim(CLAIM_TYPE);
      return TYPE_REFRESH.equals(type);
    } catch (ParseException e) {
      return false;
    }
  }

  public ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Lax")
        .path("/")
        .maxAge(refreshTokenValidity)
        .build();
  }

  public ResponseCookie expireRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Lax")
        .path("/")
        .maxAge(0)
        .build();
  }
}
