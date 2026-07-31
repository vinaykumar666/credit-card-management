package com.cards.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties under {@code app.oauth2} for token lifetimes and registered clients.
 * Bound from application YAML and used by the Authorization Server and token issuer.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2ClientProperties {

    /** Access token lifetime in minutes (default 15). */
    private long accessTokenTtlMinutes = 15;

    /** Refresh token lifetime in days (default 7). */
    private long refreshTokenTtlDays = 7;

    /** OAuth2 clients registered with the Authorization Server. */
    private List<Client> clients = new ArrayList<>();

    /**
     * One OAuth2 client entry: credentials, redirects, grants, scopes, and PKCE requirement.
     */
    @Getter
    @Setter
    public static class Client {

        /** Public client identifier. */
        private String clientId;

        /** Client secret (may use a password-encoder id prefix such as {@code {noop}}). */
        private String clientSecret;

        /** Allowed redirect URIs for authorization-code flows. */
        private List<String> redirectUris = new ArrayList<>();

        /** Allowed grant types (for example {@code authorization_code}, {@code refresh_token}). */
        private List<String> grantTypes = new ArrayList<>();

        /** Allowed OAuth2/OIDC scopes. */
        private List<String> scopes = new ArrayList<>();

        /** When true, authorization-code clients must use PKCE. */
        private boolean requirePkce;
    }
}
