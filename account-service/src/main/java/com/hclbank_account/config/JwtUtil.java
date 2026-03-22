package com.hclbank_account.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims validateAndExtract(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody();
    }

    // Extract internal customer UUID — used to scope all DB queries
    public UUID extractCustomerUUID(String token) {
        return UUID.fromString(
                (String) validateAndExtract(token).get("id"));
    }
}