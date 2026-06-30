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
import jakarta.annotation.PostConstruct;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  @Value("${discodeit.security.jwt.secret}")
  private String secretKey;

  @Value("${discodeit.security.jwt.access-token-validity}")
  private long accessTokenValidity;

  @Value("${discodeit.security.jwt.refresh-token-validity}")
  private long refreshTokenValidity;

  private JWSSigner signer;
  private JWSVerifier verifier;

  @PostConstruct
  public void init() {
    try {
      this.signer = new MACSigner(secretKey.getBytes());
      this.verifier = new MACVerifier(secretKey.getBytes());
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 서명/검증기 초기화 실패", e);
    }
  }

  public String createAccessToken(UUID userId, String username) {
    return createToken(userId, username, accessTokenValidity);
  }

  public String createRefreshToken(UUID userId) {
    return createToken(userId, null, refreshTokenValidity);
  }

  private String createToken(UUID userId, String username, long validitySeconds) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + validitySeconds * 1000);

    JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
        .subject(userId.toString())
        .issueTime(now)
        .expirationTime(expiry);

    if (username != null) {
      claimsBuilder.claim("username", username);
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
      return signedJWT.getJWTClaimsSet().getStringClaim("username");
    } catch (ParseException e) {
      throw new IllegalArgumentException("유효하지 않은 토큰입니다.", e);
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
    } catch (JOSEException | ParseException e) {
      return false;
    }
  }
}
