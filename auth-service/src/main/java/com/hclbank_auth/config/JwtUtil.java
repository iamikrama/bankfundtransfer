package com.hclbank_auth.config;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Called after signup and login
    // Puts customerId (CUST000001) as subject
    // Puts internal UUID as "id" claim
    // Other services extract UUID from "id" claim to scope DB queries
    public String generateToken(String customerId, UUID internalId) {
        return Jwts.builder()
                .setSubject(customerId)
                .claim("id", internalId.toString())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateAndExtract(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody();
    }
}