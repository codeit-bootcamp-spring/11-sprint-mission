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
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_USERNAME = "username";
  private static final String CLAIM_ROLE = "role";
  private static final String TOKEN_TYPE_ACCESS = "access";
  private static final String TOKEN_TYPE_REFRESH = "refresh";
  private static final String CLAIM_TOKEN_TYPE = "tokenType";

  private final JWSSigner signer;
  private final JWSVerifier verifier;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtTokenProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
  ) throws JOSEException {
    byte[] secretBytes = secret.getBytes();
    this.signer = new MACSigner(secretBytes);
    this.verifier = new MACVerifier(secretBytes);
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public String generateAccessToken(UUID userId, String username, String role) {
    return generateToken(userId, username, role, accessTokenExpiration, TOKEN_TYPE_ACCESS);
  }

  public String generateRefreshToken(UUID userId, String username, String role) {
    return generateToken(userId, username, role, refreshTokenExpiration, TOKEN_TYPE_REFRESH);
  }

  private String generateToken(UUID userId, String username, String role,
      long expiration, String tokenType) {
    try {
      Date now = new Date();
      JWTClaimsSet claims = new JWTClaimsSet.Builder()
          .subject(username)
          .issueTime(now)
          .expirationTime(new Date(now.getTime() + expiration))
          .claim(CLAIM_USER_ID, userId.toString())
          .claim(CLAIM_USERNAME, username)
          .claim(CLAIM_ROLE, role)
          .claim(CLAIM_TOKEN_TYPE, tokenType)
          .build();

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
      signedJWT.sign(signer);
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new JwtTokenGenerationException("토큰 생성 실패", e);
    }
  }

  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      if (!signedJWT.verify(verifier)) {
        return false;
      }
      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
      return expiration != null && expiration.after(new Date());
    } catch (ParseException | JOSEException e) {
      log.debug("토큰 유효성 검사 실패: {}", e.getMessage());
      return false;
    }
  }

  public JWTClaimsSet parseClaims(String token) {
    try {
      return SignedJWT.parse(token).getJWTClaimsSet();
    } catch (ParseException e) {
      throw new RuntimeException("토큰 파싱 실패", e);
    }
  }

  public UUID getUserId(String token) {
    try {
      return UUID.fromString((String) parseClaims(token).getClaim(CLAIM_USER_ID));
    } catch (Exception e) {
      throw new RuntimeException("userId 추출 실패", e);
    }
  }

  public String getUsername(String token) {
    return (String) parseClaims(token).getClaim(CLAIM_USERNAME);
  }

  public String getRole(String token) {
    return (String) parseClaims(token).getClaim(CLAIM_ROLE);
  }

  public long getRefreshTokenExpiration() {
    return refreshTokenExpiration;
  }

  public Date getExpirationTime(String token) {
    try {
      return parseClaims(token).getExpirationTime();
    } catch (Exception e) {
      throw new RuntimeException("만료 시간 추출 실패", e);
    }
  }
}