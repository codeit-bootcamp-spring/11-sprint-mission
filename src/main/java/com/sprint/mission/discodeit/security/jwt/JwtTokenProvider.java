package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private final String secretKey;
  private final long accessTokenExpiration;
  @Getter
  private final long refreshTokenExpiration;

  public JwtTokenProvider(@Value("${discodeit.jwt.secret}") String secretKey,
      @Value("${discodeit.jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${discodeit.jwt.refresh-token-expiration}") long refreshTokenExpiration) {
    this.secretKey = secretKey;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }


  // 토큰 발급 ──────────────────────────────────────────────
  public String generateAccessToken(DiscodeitUserDetails userDetails) throws JOSEException {
    String subject = userDetails.getUsername();

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(subject)
        .issueTime(new Date())
        .expirationTime(new Date(System.currentTimeMillis() + accessTokenExpiration))
        .build();

    JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

    SignedJWT signedJWT = new SignedJWT(header, claimsSet);

    signedJWT.sign(new MACSigner(secretKey.getBytes()));

    return signedJWT.serialize();
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) throws JOSEException {
    String subject = userDetails.getUsername();

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(subject)
        .issueTime(new Date())
        .expirationTime(new Date(System.currentTimeMillis() + refreshTokenExpiration))
        .build();

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
    signedJWT.sign(new MACSigner(secretKey.getBytes()));

    return signedJWT.serialize();
  }

  //  유효성 검사 ────────────────────────────────────────────
  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      JWSVerifier verifier = new MACVerifier(secretKey.getBytes());
      if (!signedJWT.verify(verifier)) {
        return false;
      }

      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expirationTime != null && expirationTime.before(new Date())) {
        return false;
      }

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public Instant getExpiration(String token) throws ParseException {
    SignedJWT signedJWT = SignedJWT.parse(token);
    return signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
  }

  public String getSubject(String token) throws ParseException {
    SignedJWT signedJWT = SignedJWT.parse(token);
    return signedJWT.getJWTClaimsSet().getSubject();
  }

  // TODO: 토큰 갱신 (사용 안되고 있음) ──────────────────────────────────────────────
  public String reissueAccessToken(String refreshToken, DiscodeitUserDetails userDetails)
      throws JOSEException {
    if (!validateToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    return generateAccessToken(userDetails);
  }
}
