package com.cards.auth.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Configures Spring Authorization Server endpoints, registered clients, and JWT signing keys.
 * Provides the highest-priority security chain for OAuth2/OIDC protocol endpoints and JWKS.
 */
@Configuration
public class AuthorizationServerConfig {

    /**
     * Security filter chain for Authorization Server endpoints (order 1).
     * Enables OIDC defaults, JWT resource-server support, and redirects HTML clients to {@code /login}.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the Authorization Server {@link SecurityFilterChain}
     * @throws Exception if Spring Security configuration fails
     */
    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(Customizer.withDefaults());
        http.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"),
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
        ));
        http.oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Loads OAuth2 clients from {@link OAuth2ClientProperties} into an in-memory repository.
     * Applies shared access/refresh token TTLs and per-client grants, redirects, scopes, and PKCE.
     *
     * @param properties application OAuth2 client configuration
     * @return in-memory {@link RegisteredClientRepository}
     */
    @Bean
    RegisteredClientRepository registeredClientRepository(OAuth2ClientProperties properties) {
        List<RegisteredClient> clients = new ArrayList<>();
        for (OAuth2ClientProperties.Client client : properties.getClients()) {
            RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(client.getClientId())
                    .clientSecret(client.getClientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(properties.getAccessTokenTtlMinutes()))
                            .refreshTokenTimeToLive(Duration.ofDays(properties.getRefreshTokenTtlDays()))
                            .reuseRefreshTokens(false)
                            .build())
                    .clientSettings(ClientSettings.builder()
                            .requireProofKey(client.isRequirePkce())
                            .requireAuthorizationConsent(false)
                            .build());

            for (String grant : client.getGrantTypes()) {
                builder.authorizationGrantType(new AuthorizationGrantType(grant));
            }
            for (String uri : client.getRedirectUris()) {
                builder.redirectUri(uri);
            }
            for (String scope : client.getScopes()) {
                if (OidcScopes.OPENID.equals(scope) || OidcScopes.PROFILE.equals(scope)
                        || "cards.read".equals(scope) || "cards.write".equals(scope)) {
                    builder.scope(scope);
                } else {
                    builder.scope(scope);
                }
            }
            clients.add(builder.build());
        }
        return new InMemoryRegisteredClientRepository(clients);
    }

    /**
     * Exposes the RSA key pair as a JWK source used for signing and JWKS discovery.
     *
     * @param rsaKeyProperties holder of the RSA public/private key pair
     * @return immutable JWK source with key id {@code cards-auth-key}
     */
    @Bean
    JWKSource<SecurityContext> jwkSource(RsaKeyProperties rsaKeyProperties) {
        RSAKey rsaKey = new RSAKey.Builder(rsaKeyProperties.publicKey())
                .privateKey(rsaKeyProperties.privateKey())
                .keyID("cards-auth-key")
                .build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    /**
     * Creates a JWT decoder that validates tokens against the Authorization Server JWK set.
     *
     * @param jwkSource the signing key source
     * @return JWT decoder for resource-server and validation flows
     */
    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Creates a JWT encoder that signs tokens with the same JWK set.
     *
     * @param jwkSource the signing key source
     * @return Nimbus-based JWT encoder
     */
    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Builds Authorization Server settings with the configured issuer URI.
     *
     * @param issuer issuer URL from {@code spring.security.oauth2.authorizationserver.issuer}
     * @return Authorization Server settings
     */
    @Bean
    AuthorizationServerSettings authorizationServerSettings(
            @Value("${spring.security.oauth2.authorizationserver.issuer}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }
}
