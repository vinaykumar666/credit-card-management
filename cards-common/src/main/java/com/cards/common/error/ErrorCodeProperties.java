package com.cards.common.error;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Spring configuration properties that hold the shared error-code catalog.
 * Bound from {@code error.codes} in {@code error-codes.yml} (prefix {@code error}).
 */
@ConfigurationProperties(prefix = "error")
public class ErrorCodeProperties {

    /**
     * Why LinkedHashMap: preserves YAML declaration order for debugging/docs dumps.
     * SequencedMap (Java 21) semantics via LinkedHashMap implementation.
     */
    private Map<String, ErrorCodeDefinition> codes = new LinkedHashMap<>();

    /**
     * Returns the mutable map of error code to definition (used by Spring binding).
     *
     * @return map of catalog codes to {@link ErrorCodeDefinition}
     */
    public Map<String, ErrorCodeDefinition> getCodes() {
        return codes;
    }

    /**
     * Replaces the catalog map. Null is treated as an empty map.
     *
     * @param codes new code definitions, or null for empty
     */
    public void setCodes(Map<String, ErrorCodeDefinition> codes) {
        this.codes = codes != null ? codes : new LinkedHashMap<>();
    }

    /**
     * Looks up a definition for the given code without throwing.
     *
     * @param code catalog key such as {@code AUTH_001}
     * @return the definition if present, otherwise empty
     */
    public Optional<ErrorCodeDefinition> find(String code) {
        return Optional.ofNullable(codes.get(code));
    }

    /**
     * Returns the definition for the given code, or a fallback 500 "Unknown error code" entry.
     *
     * @param code catalog key such as {@code AUTH_001}
     * @return matching definition, or a safe default when the code is missing
     */
    public ErrorCodeDefinition require(String code) {
        return find(code).orElse(new ErrorCodeDefinition(500, "Unknown error code: " + code));
    }

    /**
     * Returns an unmodifiable view of the catalog for read-only use.
     *
     * @return unmodifiable map of all loaded error codes
     */
    public Map<String, ErrorCodeDefinition> asUnmodifiable() {
        return Collections.unmodifiableMap(codes);
    }
}
