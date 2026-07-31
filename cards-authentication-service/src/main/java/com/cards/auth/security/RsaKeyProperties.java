package com.cards.auth.security;

import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Holds an RSA key pair used to sign and verify JWTs for this service.
 * Generates a 2048-bit pair for local/dev; production should inject keys via K8s Secret or HSM.
 * RSA (instead of shared HMAC) enables JWKS discovery for resource servers and future key rotation.
 */
@Component
public class RsaKeyProperties {

    private final KeyPair keyPair;

    /**
     * Creates the component and generates a new RSA key pair at startup.
     */
    public RsaKeyProperties() {
        this.keyPair = generate();
    }

    /**
     * Returns the RSA public key used for verification and JWKS exposure.
     *
     * @return the public key
     */
    public RSAPublicKey publicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }

    /**
     * Returns the RSA private key used for signing JWTs.
     *
     * @return the private key
     */
    public RSAPrivateKey privateKey() {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    /**
     * Generates a new 2048-bit RSA key pair.
     *
     * @return the generated key pair
     * @throws IllegalStateException if the JVM cannot create an RSA key pair
     */
    private static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate RSA key pair", ex);
        }
    }
}
