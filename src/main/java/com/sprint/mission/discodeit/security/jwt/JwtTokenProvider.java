package com.sprint.mission.discodeit.security.jwt;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component

public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private static final JWSAlgorithm SIGNATURE_ALGORITHM = JWSAlgorithm.HS256;

  private static final String CLAIM_ROLE = "role";

  @Value("${discodeit.jwt.secret}")
  private String secret;
  @Value("${discodeit.jwt.access-token-expiration}")
  private long accessTokenExpirationMillis;
  @Value("${discodeit.jwt.refresh-token-expiration}")
  private long refreshTokenExpirationMillis;
  private MACSigner signer;
  private MACVerifier verifier;

  @PostConstruct
  public void init() {
    byte[] secretKey = secret.getBytes(StandardCharsets.UTF_8);
    try {
      this.signer = new MACSigner(secretKey);
      this.verifier = new MACVerifier(secretKey);
    } catch (JOSEException e) {
      throw new IllegalStateException(
          "JWT secret 키가 유효하지 않습니다. HS256은 최소 32바이트 이상이어야 합니다.", e);
    }
  }

  // 토큰 발급

  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(accessTokenExpirationMillis);

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(String.valueOf(userDetails.getUserDto().id()))
        .issueTime(Date.from(now))
        .expirationTime(Date.from(expiration))
        .claim(CLAIM_ROLE, String.valueOf(userDetails.getUserDto().role()))
        .build();

    JWSHeader header = new JWSHeader(SIGNATURE_ALGORITHM);

    SignedJWT signedJWT = new SignedJWT(header, claims);

    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new IllegalStateException("토큰 서명에 실패했습니다.", e);
    }
    return signedJWT.serialize();
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(refreshTokenExpirationMillis);

    //payload
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(String.valueOf(userDetails.getUserDto().id()))
        .issueTime(Date.from(now))
        .expirationTime(Date.from(expiration))
        .build();

    //header
    JWSHeader header = new JWSHeader(SIGNATURE_ALGORITHM);

    SignedJWT signedJWT = new SignedJWT(header, claims);

    //signature
    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new IllegalStateException("토큰 서명에 실패했습니다.", e);
    }

    // 직렬화
    return signedJWT.serialize();
  }

  //토큰 검증
  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 서명 검증
      if (!signedJWT.verify(verifier)) {
        return false;
      }

      // 만료 검증
      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
      return expiration != null && expiration.after(new Date());
    } catch (ParseException | JOSEException e) {
      return false;
    }
  }


  public Instant getExpiration(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
    } catch (ParseException e) {
      throw new IllegalArgumentException("잘못된 형식의 토큰입니다.", e);
    }
  }

  public String getSubject(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new IllegalArgumentException("잘못된 형식의 토큰입니다.", e);
    }
  }

  //토큰 재발급
  public String reissueAccessToken(String refreshToken) {
    // Refresh Token 유효성 검사
    if (!validateToken(refreshToken)) {
      throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
    }

    // subject를 꺼내 새 Access Token 생성
    String subject = getSubject(refreshToken);

    Instant now = Instant.now();
    Instant expiration = now.plusMillis(accessTokenExpirationMillis);

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(subject)
        .issueTime(Date.from(now))
        .expirationTime(Date.from(expiration))
        .build();

    JWSHeader header = new JWSHeader(SIGNATURE_ALGORITHM);
    SignedJWT signedJWT = new SignedJWT(header, claims);

    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new IllegalStateException("토큰 서명에 실패했습니다.", e);
    }

    return signedJWT.serialize();
  }

}




