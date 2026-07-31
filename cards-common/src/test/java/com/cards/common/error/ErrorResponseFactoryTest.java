package com.cards.common.error;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseFactoryTest {

    @Test
    void buildsResponseFromYamlBackedProperties() {
        ErrorCodeProperties properties = new ErrorCodeProperties();
        Map<String, ErrorCodeDefinition> codes = new LinkedHashMap<>();
        codes.put(ErrorCodes.AUTH_001, new ErrorCodeDefinition(401, "Invalid credentials"));
        properties.setCodes(codes);

        ErrorResponse response = ErrorResponseFactory.from(properties, ErrorCodes.AUTH_001, "/api/v1/auth/login");
        assertEquals(401, response.status());
        assertEquals(ErrorCodes.AUTH_001, response.errorCode());
        assertEquals("Invalid credentials", response.message());
        assertEquals("/api/v1/auth/login", response.path());
    }

    @Test
    void patternMatchesValidationDetail() {
        ErrorCodeProperties properties = new ErrorCodeProperties();
        Map<String, ErrorCodeDefinition> codes = new LinkedHashMap<>();
        codes.put(ErrorCodes.COMMON_001, new ErrorCodeDefinition(400, "Validation failed"));
        properties.setCodes(codes);

        BusinessException ex = new ValidationBusinessException(ErrorCodes.COMMON_001, "email: must not be blank");
        ErrorResponse response = ErrorResponseFactory.from(properties, ex, "/x");
        assertEquals("email: must not be blank", response.message());
    }
}
