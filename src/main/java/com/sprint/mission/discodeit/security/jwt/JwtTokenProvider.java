package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.exception.jwt.JwtException;
import com.sprint.mission.discodeit.exception.jwt.JwtExpiredException;
import com.sprint.mission.discodeit.exception.jwt.JwtGenerationFailedException;
import com.sprint.mission.discodeit.exception.jwt.JwtInvalidSignatureException;
import com.sprint.mission.discodeit.exception.jwt.JwtMalformedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final byte[] secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final DiscodeitUserDetailsService userDetailsService;

    public JwtTokenProvider(JwtProperties jwtProperties, DiscodeitUserDetailsService userDetailsService) {
        this.secretKey = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        this.accessTokenExpiration = jwtProperties.accessTokenExpiration();
        this.refreshTokenExpiration = jwtProperties.refreshTokenExpiration();
        this.userDetailsService = userDetailsService;
    }

    private String buildToken(String userId, String role, long expirationMillis) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(userId)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusMillis(expirationMillis)));
            if (role != null) {
                claimsBuilder.claim("role", role);
            }
            JWTClaimsSet claims = claimsBuilder.build();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new MACSigner(secretKey);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new JwtGenerationFailedException(e);
        }
    }

    public String generateAccessToken(DiscodeitUserDetails userDetails) {
        String userId = userDetails.getUserDto().id().toString();
        String role = userDetails.getUserDto().role().name();
        return buildToken(userId, role, accessTokenExpiration);
    }

    public String generateRefreshToken(DiscodeitUserDetails userDetails) {
        String userId = userDetails.getUserDto().id().toString();
        return buildToken(userId, null, refreshTokenExpiration);
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey);
            if (!signedJWT.verify(verifier)) {
                throw new JwtInvalidSignatureException();
            }
            if (!signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date())) {
                throw new JwtExpiredException();
            }
            return true;
        } catch (JwtExpiredException | JwtInvalidSignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtMalformedException(e);
        }
    }

    public Instant getExpiration(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime().toInstant();
        } catch (Exception e) {
            throw new JwtMalformedException(e);
        }
    }

    public String getSubject(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new JwtMalformedException(e);
        }
    }

    public String reissueAccessToken(String refreshToken) {
        validateToken(refreshToken);
        try {
            String userId = SignedJWT.parse(refreshToken).getJWTClaimsSet().getSubject();
            DiscodeitUserDetails userDetails = userDetailsService.loadUserById(UUID.fromString(userId));
            return generateAccessToken(userDetails);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtMalformedException(e);
        }
    }
}
