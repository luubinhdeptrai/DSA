package com.example.bookcatalog.security;

import com.example.bookcatalog.dto.AccessTokenResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final String issuer;
    private final String audience;
    private final long ttlSeconds;

    public TokenService(
            JwtEncoder encoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience,
            @Value("${app.jwt.access-token-ttl-seconds}") long ttlSeconds) {
        if (ttlSeconds < 1) {
            throw new IllegalArgumentException("JWT lifetime must be positive");
        }
        this.encoder = encoder;
        this.issuer = issuer;
        this.audience = audience;
        this.ttlSeconds = ttlSeconds;
    }

    public AccessTokenResponse issue(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Trusted authentication required");
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.equals("ROLE_USER") || a.equals("ROLE_ADMIN"))
                .map(a -> a.substring("ROLE_".length()))
                .distinct()
                .sorted()
                .toList();
        if (roles.isEmpty()) {
            throw new IllegalStateException("Authenticated user has no supported role");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(authentication.getName())
                .audience(List.of(audience))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String signedJwt = encoder.encode(
                JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AccessTokenResponse(signedJwt, "Bearer", ttlSeconds);
    }
}
