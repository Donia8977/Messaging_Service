//package com.example.MessageService.security.jwt;//package security.jwt;
//
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.function.Function;
//
//@Component
//public class JwtUtil {
//
//    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
//
//    @Value("${security.jwt.secret}")
//    private String jwtSecret;
//
//    @Value("${security.jwt.expiration-ms}")
//    private long jwtExpirationMs;
//
//
//    /**
//     * Decode the Base64-encoded secret into raw bytes,
//     * then build a SecretKey suitable for HS512.
//     */
//    private Key getSigningKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//    /**
//     * Generate a JWT token containing the username as subject, issued at current time,
//     * and expiring after {@code jwtExpirationMs} milliseconds.
//     */
//    public String generateToken(String username) {
//        Date now = new Date();
//        Date expiry = new Date(now.getTime() + jwtExpirationMs);
//
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(now)
//                .setExpiration(expiry)
//                .signWith(SignatureAlgorithm.HS512, jwtSecret)
//                .compact();
//    }
//
//    /**
//     * Extract username (subject) from the token.
//     */
//    public String getUsernameFromToken(String token) {
//        return getClaim(token, Claims::getSubject);
//    }
//
//    /**
//     * Extract expiration date from the token.
//     */
//    public Date getExpirationDateFromToken(String token) {
//        return getClaim(token, Claims::getExpiration);
//    }
//
//    /**
//     * Generic method to extract any claim from token using a claims resolver function.
//     */
//    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
//        Claims claims = parseAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    /**
//     * Parse the JWT and retrieve all claims.
//     */
//    private Claims parseAllClaims(String token) {
//        try {
//            return Jwts.parser()
//                    .setSigningKey(jwtSecret)
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (Exception ex) {
//            log.error("Failed to parse JWT token: {}", ex.getMessage());
//            throw ex;
//        }
//    }
//
//    /**
//     * Validate the token: checks username match and expiration.
//     */
//    public boolean validateToken(String token, String username) {
//        final String tokenUsername = getUsernameFromToken(token);
//        return (tokenUsername.equals(username) && !isTokenExpired(token));
//    }
//
//    /**
//     * Check if the token has expired.
//     */
//    private boolean isTokenExpired(String token) {
//        return getExpirationDateFromToken(token).before(new Date());
//    }
//}

package com.example.MessageService.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // Decode the Base64-encoded secret into raw bytes, then build an HMAC-SHA key
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
