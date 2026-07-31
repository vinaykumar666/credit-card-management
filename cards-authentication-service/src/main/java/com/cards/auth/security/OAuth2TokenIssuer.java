package com.cards.auth.security;

import com.cards.auth.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Issues Authorization Server–compatible JWTs for first-party login and register flows.
 * Signs tokens with the same JWKS key material used by the OAuth2 Authorization Server.
 */
@Service
public class OAuth2TokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final OAuth2ClientProperties properties;
    private final String issuer;

    /**
     * Creates a token issuer wired to the JWT encoder, TTL settings, and issuer URL.
     *
     * @param jwtEncoder JWT encoder backed by the service JWK set
     * @param properties OAuth2 client and token TTL settings
     * @param issuer     Authorization Server issuer URI from configuration
     */
    public OAuth2TokenIssuer(JwtEncoder jwtEncoder,
                             OAuth2ClientProperties properties,
                             @Value("${OAUTH2_ISSUER:http://localhost:8081}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.issuer = issuer;
    }

    /**
     * Builds and signs an RS256 access token for the given user.
     * Claims include subject (user id), email, full name, roles, audience, and scopes.
     *
     * @param user the user for whom the access token is issued
     * @return compact JWT access token string
     */
    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getAccessTokenTtlMinutes() * 60);
        List<String> roles = user.getRoles().stream().map(role -> role.getName()).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(user.getId().toString())
                .audience(List.of("cards-platform"))
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("roles", roles)
                .claim("scope", "openid profile cards.read cards.write")
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId("cards-auth-key").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
