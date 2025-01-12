package com.example.chatAppServer.token;

import com.example.chatAppServer.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class TokenHelper {
    private static final String SECRET_KEY = "yourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKey";
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days

    public static String generateToken(UserEntity userEntity) {
        Date now = new Date();
        Date expirrationDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .claim("user_id",userEntity.getId())
                .claim("username",userEntity.getUsername())
                .setSubject(userEntity.getUsername())
                .setIssuedAt(now)
                .setExpiration(expirrationDate)
                .signWith(SignatureAlgorithm.HS512,SECRET_KEY)
                .compact();
    }

    public static Long getUserIdFromToken(String accessToken) {
        accessToken = accessToken.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(accessToken)
                .getBody();
        return claims.get("user_id", Long.class);
    }
}
