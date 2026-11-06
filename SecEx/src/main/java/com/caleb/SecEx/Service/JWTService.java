package com.caleb.SecEx.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

@Service 
public class JWTService {

    private String secretKey = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    public String generateToken(String username) {

        Map<String, Object> claims = new HashMap<>();
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + 60 * 60 * 20))
            .signWith(getKey())
            .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
return "";
    }

}
