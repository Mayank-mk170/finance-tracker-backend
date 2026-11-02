
package com.finance.tracker.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt.algorithm.key}")
    private String algorithmKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiry.duration}")
    private long expiryTime;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void postConstruct() {
        if (algorithmKey == null || algorithmKey.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret must contain at least 32 characters"
            );
        }

        algorithm = Algorithm.HMAC256(algorithmKey);

        verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    public String generateToken(String userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryTime);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(userId)
                .withClaim("email", email)
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .sign(algorithm);
    }

    public String getUserId(String token) {
        return verifyToken(token).getSubject();
    }

    public String getEmail(String token) {
        return verifyToken(token).getClaim("email").asString();
    }

    public String getUserName(String token) {
        return verifyToken(token).getClaim("name").asString();
    }

    public DecodedJWT verifyToken(String token) {
        return verifier.verify(token);
    }
}
