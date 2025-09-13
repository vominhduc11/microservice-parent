package com.devwonder.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AuthJwtService {

    private final JwksService jwksService;

    // Access token expiration time: 30 minutes
    private static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000;
    
    // Refresh token expiration time: 7 days
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    public String generateToken(String username, Map<String, Object> claims) {
        Map<String, Object> accessTokenClaims = new HashMap<>(claims);
        accessTokenClaims.put("token_type", "access");
        return createToken(accessTokenClaims, username);
    }

    public String generateToken(String username) {
        return generateToken(username, Map.of());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return createToken(claims, subject, ACCESS_TOKEN_EXPIRATION);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .setHeaderParam("kid", jwksService.getKeyId())
                .signWith(jwksService.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(String username, Map<String, Object> claims) {
        Map<String, Object> refreshTokenClaims = new HashMap<>(claims);
        refreshTokenClaims.put("token_type", "refresh");
        return createToken(refreshTokenClaims, username, REFRESH_TOKEN_EXPIRATION);
    }

    public long getAccessTokenExpirationInSeconds() {
        return ACCESS_TOKEN_EXPIRATION / 1000;
    }

    public long getRefreshTokenExpirationInSeconds() {
        return REFRESH_TOKEN_EXPIRATION / 1000;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("token_type", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwksService.getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Validate token with option to allow expired tokens
     * Used for token refresh where we need to validate signature but allow expired tokens
     */
    public Boolean isTokenValid(String token, String username, boolean allowExpired) {
        try {
            final String tokenUsername = extractUsername(token);
            if (!tokenUsername.equals(username)) {
                return false;
            }
            
            if (allowExpired) {
                // Only check signature and format, ignore expiration
                return true;
            } else {
                // Normal validation including expiration check
                return !isTokenExpired(token);
            }
        } catch (Exception e) {
            // Invalid token format or signature
            return false;
        }
    }

    /**
     * Validate refresh token specifically - must be valid and not expired
     */
    public Boolean isRefreshTokenValid(String token, String username) {
        try {
            // Check if token is refresh token
            String tokenType = extractTokenType(token);
            if (!"refresh".equals(tokenType)) {
                return false;
            }
            
            // Check username matches
            final String tokenUsername = extractUsername(token);
            if (!tokenUsername.equals(username)) {
                return false;
            }
            
            // Refresh token MUST NOT be expired
            return !isTokenExpired(token);
            
        } catch (Exception e) {
            // Invalid token format or signature
            return false;
        }
    }
}